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
                .collect(Collectors.toList());
    }

    private List<MissionReferenceDoc> getMissionRefsByGroupId(String groupId) {
        return getQueryResult(firestore.collection(groupId)).stream()
                .map(MissionReferenceDoc::fromDocument)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<MissionDoc> getMissionsByRef(MissionReferenceDoc ref) {
        return getQueryResult(MissionReferenceDoc.getTargetMissions(ref, firestore)).stream()
                .map(MissionDoc::fromDocument)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
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
