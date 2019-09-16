package org.mozilla.msrp.platform.common.firebase

import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.FieldValue
import com.google.cloud.firestore.SetOptions
import org.mozilla.msrp.platform.firestore.getResultsUnchecked
import org.mozilla.msrp.platform.firestore.getUnchecked
import org.mozilla.msrp.platform.firestore.setUnchecked

class DistributedCounter(
        private val counterRef: CollectionReference,
        private val numShards: Int
) {
    val count: Int
        get() {
            return counterRef.getResultsUnchecked().sumBy {
                it.toObject(ShardDoc::class.java).count
            }
        }

    fun increase() = add(1)

    fun decrease() = add(-1)

    private fun add(value: Int) {
        val shardId = Math.floor(Math.random() * numShards).toInt()
        val shardRef = counterRef.document("$shardId")
        shardRef.update("count", FieldValue.increment(value.toLong()))
    }

    data class ShardDoc(val count: Int = 0)
}

fun DocumentReference.setupCounter(counterName: String, numShards: Int) {
    // Put a field in the document to indicate how many shards are used for the counter
    this.set(
            mapOf(getNumShardsFieldName(counterName) to numShards),
            SetOptions.merge()
    ).get()

    // Init count=0 for all shards
    val counterRef = this.collection(getShardsCollectionName(counterName))
    for (i in 0 until numShards) {
        counterRef.document(i.toString()).setUnchecked(DistributedCounter.ShardDoc(0))
    }
}

fun DocumentReference.getCounter(counterName: String): DistributedCounter? {
    val numShards = this.getUnchecked()
            .getLong(getNumShardsFieldName(counterName))
            ?.toInt() ?: return null

    val counterRef = this.collection(getShardsCollectionName(counterName))
    return DistributedCounter(counterRef, numShards)
}

private fun getNumShardsFieldName(counterName: String): String {
    return "numShards_$counterName"
}

private fun getShardsCollectionName(counterName: String): String {
    return "shards_$counterName"
}

