from datetime import datetime, timedelta
import pytest
import requests
import math
import json
import psycopg2
import logging
import os
from time import sleep

LOGGER = logging.getLogger(__name__)

pytest.SERVER = "https://rocket-dev01.appspot.com"
pytest.FAKE_STMO_ENDPOINT = "/test/uids?times=1"
pytest.EQUEUE_PUSH_REQUEST_ENDPOINT = "/api/v1/admin/push/enqueue"


@pytest.fixture
def clean_db_entry(request):
    '''
    setup database and tear down to clear up db testing data
    '''
    CLOUD_DB = {}
    CLOUD_DB['name'] = request.config.getoption("--CLOUD_DB_NAME")
    CLOUD_DB['user'] = request.config.getoption("--CLOUD_DB_USER")
    CLOUD_DB['pwd'] = request.config.getoption("--CLOUD_DB_PWD")
    conn = connect_database(CLOUD_DB)
    conn_info = []
    conn_info.append(conn)
    yield conn_info
    LOGGER.info(
        'Clear up database entry where mozMessageId [{}]'.format(
            conn_info[1]))
    doDelete(conn, conn_info[1])
    conn.close()


def connect_database(CLOUD_DB):
    """Connect to local database through proxy
    """
    hostname = '127.0.0.1'
    conn = psycopg2.connect(
        host=hostname,
        user=CLOUD_DB['user'],
        password=CLOUD_DB['pwd'],
        dbname=CLOUD_DB['name'])
    return conn


def doQuery(conn, mozMessageId):
    """Perform query to get the number of jobs under the target moz_msg_id
    """
    cur = conn.cursor()
    cur.execute(
        "select count(id) from push where moz_msg_id= '{}' ".format(mozMessageId))
    for total_number in cur.fetchall():
        LOGGER.info(
            'Get total count {} where mozMessageId [{}] '.format(
                total_number, mozMessageId))
        return (total_number)


def doDelete(conn, mozMessageId):
    """Perform query to clearup jobs under the target moz_msg_id
    """
    cur = conn.cursor()
    cur.execute(
        "delete from push where moz_msg_id= '{}' ".format(mozMessageId))
    conn.commit()
    LOGGER.info(
        'Successfully delete entry where mozMessageId [{}] '.format(mozMessageId))


def enqueuePushRequest(pushDeepLink, pushOpenUrl):
    """equeue push request with proper parameters
    """
    target_time = datetime.now() + timedelta(minutes=5)
    target_timestamp_millisec = math.ceil(
        datetime.timestamp(target_time) * 1000)
    mozMessageId = "auto-test-mozMessageId-" + str(target_timestamp_millisec)
    title = "auto-test-title-" + str(target_timestamp_millisec)

    data_dic = {}
    data_dic["token"] = "auto-test-token"
    data_dic["stmoUrl"] = pytest.SERVER + pytest.FAKE_STMO_ENDPOINT
    data_dic["title"] = title
    data_dic["destination"] = "auto-test-destination"
    data_dic["displayType"] = "auto-test-displayType"
    data_dic["displayTimestamp"] = target_timestamp_millisec
    data_dic["mozMessageId"] = mozMessageId
    data_dic["mozMsgBatch"] = "auto-test-mozMsgBatch-" + \
        str(target_timestamp_millisec)
    data_dic["appId"] = "org.mozilla.rocket"
    data_dic["imageUrl"] = "https://homepages.cae.wisc.edu/~ece533/images/tulips.png"
    data_dic["body"] = "mock_body"

    if pushDeepLink and pushOpenUrl is None:
        data_dic['pushDeepLink'] = pushDeepLink
    if pushOpenUrl and pushDeepLink is None:
        data_dic['pushOpenUrl'] = pushOpenUrl

    data_saved = json.dumps(data_dic)
    data_json = json.loads(data_saved)

    resp = requests.post(
        pytest.SERVER +
        pytest.EQUEUE_PUSH_REQUEST_ENDPOINT,
        data=data_json)

    if resp.status_code == 200 and mozMessageId in resp.text and title in resp.text:
        LOGGER.info(
            'Successfully enqueue request to Google Cloud Message Queue where mozMessageId [{}]'.format(mozMessageId)
        )
        return mozMessageId
    else:
        LOGGER.error(
            'Failed to enqueue the request to Google Cloud Message Queue' +
            resp.text)


def getFakeStmo():
    """get mock stmo data (sql.telemetry.mozilla.org)
    """
    resp = requests.get(pytest.SERVER + pytest.FAKE_STMO_ENDPOINT)
    if resp.status_code == 200:
        LOGGER.info('Successfully cteated mock Stmo.')
    else:
        LOGGER.error('Failed to get mock Stmo' + resp.text)


@pytest.mark.parametrize("pushDeepLink", ["rocket://content/game"])
def test_openDeeplink(pushDeepLink, clean_db_entry):
    """test push notification with deeplink message
    """
    getFakeStmo()
    # Add some wait time to evoke first instance
    sleep(1.0)
    mozMessageId = enqueuePushRequest(pushDeepLink, None)
    clean_db_entry.append(mozMessageId)
    # Add some wait time to do worker job
    sleep(1.0)
    total_number = doQuery(clean_db_entry[0], mozMessageId)
    assert total_number == (1,)


@pytest.mark.parametrize("pushOpenUrl", ["https://www.facebook.com/"])
def test_openUrl(pushOpenUrl, clean_db_entry):
    """test push notification with open url message
    """
    getFakeStmo()
    # Add some wait time to evoke first instance
    sleep(1.0)
    mozMessageId = enqueuePushRequest(None, pushOpenUrl)
    clean_db_entry.append(mozMessageId)
    # Add some wait time to do worker job
    sleep(1.0)
    total_number = doQuery(clean_db_entry[0], mozMessageId)
    assert total_number == (1,)
