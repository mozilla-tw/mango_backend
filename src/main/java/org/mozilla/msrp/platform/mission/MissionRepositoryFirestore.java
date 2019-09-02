package org.mozilla.msrp.platform.mission;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import lombok.extern.log4j.Log4j2;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Named
@Log4j2

public class MissionRepositoryFirestore implements MissionRepository {

    private Firestore firestore;

    static {

        log.info("-----MissionRepositoryFirestore-----");
    }
    @Inject
    MissionRepositoryFirestore(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public List<MissionDoc> getMissionsByGroupId(String groupId) {
        return getMissionRefsByGroupId(groupId).stream()
                .map(this::getMissionsByRef)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<MissionReferenceDoc> getMissionRefsByGroupId(String groupId) {
        return getQueryResult(firestore.collection(groupId)).stream()
                .map(MissionReferenceDoc::fromDocument)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<MissionDoc> getMissionsByRef(MissionReferenceDoc ref) {
        try {
            DocumentSnapshot snapshot = firestore.collection(ref.getType()).document(ref.getMid()).get().get();
            return MissionDoc.fromDocument(snapshot);

        } catch (InterruptedException | ExecutionException e) {
            return Optional.empty();
        }
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
