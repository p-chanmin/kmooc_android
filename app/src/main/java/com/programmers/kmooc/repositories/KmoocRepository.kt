package com.programmers.kmooc.repositories

import android.util.Log
import com.programmers.kmooc.models.Lecture
import com.programmers.kmooc.models.LectureList
import com.programmers.kmooc.network.HttpClient
import com.programmers.kmooc.utils.DateUtil
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.util.*

class KmoocRepository {

    /**
     * 국가평생교육진흥원_K-MOOC_강좌정보API
     * https://www.data.go.kr/data/15042355/openapi.do
     */

    private val httpClient = HttpClient("http://apis.data.go.kr/B552881/kmooc")
    private val serviceKey =
        "LwG%2BoHC0C5JRfLyvNtKkR94KYuT2QYNXOT5ONKk65iVxzMXLHF7SMWcuDqKMnT%2BfSMP61nqqh6Nj7cloXRQXLA%3D%3D"

    fun list(completed: (LectureList) -> Unit) {
        httpClient.getJson(
            "/courseList",
            mapOf("serviceKey" to serviceKey, "Mobile" to 1)
        ) { result ->
            result.onSuccess {
                completed(parseLectureList(JSONObject(it)))
            }
        }
    }

    fun next(currentPage: LectureList, completed: (LectureList) -> Unit) {
        val nextPageUrl = currentPage.next
        httpClient.getJson(nextPageUrl, emptyMap()) { result ->
            result.onSuccess {
                completed(parseLectureList(JSONObject(it)))
            }
        }
    }

    fun detail(courseId: String, completed: (Lecture) -> Unit) {
        httpClient.getJson(
            "/courseDetail",
            mapOf("CourseId" to courseId, "serviceKey" to serviceKey)
        ) { result ->
            result.onSuccess {
                completed(parseLecture(JSONObject(it)))
            }
        }
    }

    private fun parseLectureList(jsonObject: JSONObject): LectureList {
        //TODO: JSONObject -> LectureList 를 구현하세요

        val pagination = jsonObject.getJSONObject("pagination")
        val count: Int = pagination.getInt("count")
        val numPages: Int = pagination.getInt("num_pages")
        val previous: String = pagination.getString("previous")
        val next: String = pagination.getString("next")

        return pagination.run {
            LectureList(count, numPages, previous, next,
            jsonObject.getJSONArray("results").run {
                mutableListOf<Lecture>().apply {
                    for (i in 0 until length()){
                        add(parseLecture(getJSONObject(i)))
                    }
                }
            })
        }

    }

    private fun parseLecture(jsonObject: JSONObject): Lecture {
        //TODO: JSONObject -> Lecture 를 구현하세요

        return jsonObject.run {
            Lecture(
                getString("id"),
                getString("number"),
                getString("name"),
                getString("classfy_name"),
                getString("middle_classfy_name"),
                getJSONObject("media").getJSONObject("image").getString("small"),
                getJSONObject("media").getJSONObject("image").getString("large"),
                getString("short_description"),
                getString("org_name"),
                DateUtil.parseDate(getString("start")),
                DateUtil.parseDate(getString("end")),
                if (has("teachers")) getString("teachers") else null,
                if (has("overview")) getString("overview") else null,
            )
        }

//        val id: String = jsonObject.getString("id")                 // 아이디
//        val number: String = jsonObject.getString("number")                // 강좌번호
//        val name: String = jsonObject.getString("name")               // 강좌명
//        val classfyName: String = jsonObject.getString("classfy_name")        // 강좌분류
//        val middleClassfyName: String = jsonObject.getString("middle_classfy_name")  // 강좌분류2
//        val courseImage: String = jsonObject.getJSONObject("media").getJSONObject("image").getString("small")        // 강좌 썸네일 (media>image>small)
//        val courseImageLarge: String = jsonObject.getJSONObject("media").getJSONObject("image").getString("large")   // 강좌 이미지 (media>image>large)
//        val shortDescription: String = jsonObject.getString("short_description")   // 짧은 설명
//        val orgName: String = jsonObject.getString("org_name")            // 운영기관
//        val start: Date = DateUtil.parseDate(jsonObject.getString("start"))                 // 운영기간 시작
//        val end: Date = DateUtil.parseDate(jsonObject.getString("end"))                  // 운영기간 종료
//        val teachers: String? = jsonObject.getString("teachers")          // 교수진
//        val overview: String? = jsonObject.getString("overview")
//
//        return Lecture(id, number, name, classfyName, middleClassfyName, courseImage, courseImageLarge, shortDescription, orgName, start, end, teachers, overview)

    }
}