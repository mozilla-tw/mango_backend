package org.mozilla.msrp.platform.common.firebase

import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.FieldValue
import org.mozilla.msrp.platform.firestore.getResultsUnchecked
import org.mozilla.msrp.platform.firestore.getUnchecked
import org.mozilla.msrp.platform.firestore.setUnchecked

/**
 * Each counter is structured as below
 *
 * FooDocument
 *    |- ....
 *    |- ....
 *    |- shards_catCount
 *    |      |- numShards { count = n }
 *    |      |- 0 { count = 10 }
 *    |      |- 1 { count = -5 }
 *    |      |- ...
 *    |      |- n - 1 { count = 2 }
 *    |
 *    |- shards_dogCount
 *           |- numShards { count = k }
 *           |- 0 { count = 1 }
 *           |- 1 { count = 0 }
 *           |- ...
 *           |- k - 1 { count = 2 }
 */
class DistributedCounter(
        private val counterRef: CollectionReference,
        private val numShards: Int
) {
    val count: Int
        get() {
            return counterRef.getResultsUnchecked()
                    .filterNot { it.id == NUM_SHARDS_DOC_NAME }
                    .sumBy { it.toObject(ShardDoc::class.java).count }
        }

    fun increase() = add(1)

    fun decrease() = add(-1)

    private fun add(value: Int) {
        val shardId = Math.floor(Math.random() * numShards).toInt()
        val shardRef = counterRef.document("$shardId")
        shardRef.update(SHARD_FIELD_NAME, FieldValue.increment(value.toLong()))
    }


    data class ShardDoc(val count: Int = 0)
}

fun DocumentReference.setupCounter(counterName: String, numShards: Int) {
    // Init count=0 for all shards
    val counterRef = this.collection(getShardsCollectionName(counterName))
    for (i in 0 until numShards) {
        counterRef.document(i.toString()).setUnchecked(DistributedCounter.ShardDoc(0))
    }

    counterRef.document(NUM_SHARDS_DOC_NAME).setUnchecked(mapOf(SHARD_FIELD_NAME to numShards))
}

fun DocumentReference.getCounter(counterName: String): DistributedCounter? {
    val numShardsDoc = this.collection(getShardsCollectionName(counterName)).document(NUM_SHARDS_DOC_NAME).getUnchecked()
    val numShards = numShardsDoc.getLong(SHARD_FIELD_NAME)?.toInt() ?: return null

    val counterRef = this.collection(getShardsCollectionName(counterName))
    return DistributedCounter(counterRef, numShards)
}

private fun getShardsCollectionName(counterName: String): String {
    return "shards_$counterName"
}

private const val NUM_SHARDS_DOC_NAME = "numShards"
private const val SHARD_FIELD_NAME = "count"