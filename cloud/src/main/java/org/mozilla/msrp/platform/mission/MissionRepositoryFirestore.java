package org.mozilla.msrp.platform.mission;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Named
public class MissionRepositoryFirestore implements MissionRepository {

    private Firestore firestore;

    @Inject
    MissionRepositoryFirestore(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public List<MissionDoc> getMissionsByGroupId(String groupId) {
        return getMissionRefsByGroupId(groupId).stream()
                .map(this::getMissionsByRef)
                .flatMap(Collection::stream)
                .map(this::convertToRawMission)
                .filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<QueryDocumentSnapshot> getMissionRefsByGroupId(String groupId) {
        return getQueryResult(firestore.collection(groupId));
    }

    private List<QueryDocumentSnapshot> getMissionsByRef(QueryDocumentSnapshot referenceDoc) {
        String type = referenceDoc.getString("type");
        String mid = referenceDoc.getString("mid");

        if (type == null || mid == null) {
            return new ArrayList<>();
        }

        return getQueryResult(firestore.collection(type).whereEqualTo("mid", mid));
    }

    private Optional<MissionDoc> convertToRawMission(QueryDocumentSnapshot missionDoc) {
        String mid = missionDoc.getString(MissionDoc.KEY_MID);
        String nameId = missionDoc.getString(MissionDoc.KEY_NAME_ID);
        String descriptionId = missionDoc.getString(MissionDoc.KEY_DESCRIPTION_ID);

        if (mid == null || nameId == null || descriptionId == null) {
            return Optional.empty();
        }

        return Optional.of(new MissionDoc(mid, nameId, descriptionId));
    }

    private List<QueryDocumentSnapshot> getQueryResult(Query query) {
        try {
            return query.get().get().getDocuments();

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
