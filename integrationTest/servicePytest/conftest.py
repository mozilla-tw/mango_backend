def pytest_addoption(parser):
    parser.addoption(
        "--CLOUD_DB_NAME",
        action="store",
        default="CLOUD_DB_NAME",
        help="pls provide cloud database name")
    parser.addoption(
        "--CLOUD_DB_USER",
        action="store",
        default="CLOUD_USER_NAME",
        help="pls provide cloud database username")
    parser.addoption(
        "--CLOUD_DB_PWD",
        action="store",
        default="CLOUD_DB_PWD",
        help="pls provide cloud database user password")
