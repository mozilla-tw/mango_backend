package org.mozilla.msrp.platform.app.automation

import com.google.cloud.firestore.Firestore
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.inject.Inject
import javax.inject.Named

open class ApkSizeRequest(
        var apk_size: Int = 0,
        var git_sha: String = "",
        var git_msg: String = "",
        var git_author: String = "",
        var build_id: String = ""
)

//open class ApkSizeRepositoryRequest(
//        apk_size: Int = 0,
//        git_sha: String = "",
//        git_msg: String = "",
//        git_author: String = "",
//        build_id: String = ""
//) : ApkSizeRequest(apk_size, git_sha, git_msg, git_author, build_id)
//
//class ApkSizeDocument(
//        apk_size: Int = 0,
//        git_sha: String = "",
//        git_msg: String = "",
//        git_author: String = "",
//        build_id: String = ""
//) : ApkSizeRequest(apk_size, git_sha, git_msg, git_author, build_id)

@Named
class ApkSizeRepository @Inject constructor(firestore: Firestore) {
    private val android_build = firestore.collection("android_build")

    fun log(request: ApkSizeRequest) {
        android_build.document().set(request).get()
    }

}


@RestController
class ApkSizeController @Inject constructor(private val apkSizeRepository: ApkSizeRepository){


    @GetMapping("/api/v1/build/android")
    fun logApk(@RequestParam apkSizeRequest: ApkSizeRequest):ResponseEntity<String> {
        try {
            apkSizeRepository.log(apkSizeRequest)
            return ResponseEntity.ok("done")
        }catch (e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
        }

    }


}