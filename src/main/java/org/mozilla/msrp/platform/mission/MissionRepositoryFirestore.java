package org.mozilla.msrp.platform.mission;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.DocumentReference;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
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
            String endpoint = ref.getEndpoint();
            if (endpoint.startsWith("/")) {
                String docPath = endpoint.substring(1);
                DocumentSnapshot snapshot = firestore.document(docPath).get().get();
                return MissionDoc.fromDocument(snapshot);
            } else {
                return Optional.empty();
            }

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

    @Override
    public Optional<MissionDoc> createMission(MissionCreateData createData) {
        DocumentReference docRef = firestore.collection(createData.getMissionType()).document();
        MissionDoc doc = new MissionDoc(docRef.getId(),
                createData.getMissionName(),
                createData.getTitleId(),
                createData.getDescriptionId(),
                createData.getMissionType());

        try {
            docRef.set(doc).get();
            return Optional.of(doc);

        } catch (InterruptedException | ExecutionException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<MissionReferenceDoc> groupMissions(String groupId, List<MissionGroupItemData> groupItems) {
        return groupItems.stream()
                .map(groupForm -> convertToReferenceDoc(groupId, groupForm))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<MissionReferenceDoc> convertToReferenceDoc(String groupId, MissionGroupItemData groupItem) {
        MissionReferenceDoc doc = new MissionReferenceDoc(groupItem.getEndpoint());
        try {
            firestore.collection(groupId).document().set(doc).get();
            return Optional.of(doc);

        } catch (InterruptedException | ExecutionException e) {
            return Optional.empty();
        }
    }
}
