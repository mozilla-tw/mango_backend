package org.mozilla.msrp.platform.vertical.shopping

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.mozilla.msrp.platform.util.hash
import org.mozilla.msrp.platform.vertical.game.UiModel
import org.springframework.stereotype.Repository
import java.util.UUID
import javax.inject.Inject

private val voucherString = "[    {       \"source\":\"bukalapak\",       \"name\":\"Pulsa\",       \"img\":\"image_pulsa\",       \"url\":\"http://bukalapak.go2cloud.org/aff_c?offer_id=15&aff_id=4287&aff_sub=ticket&url=https%3A%2F%2Fwww.bukalapak.com%2Fpulsa%3Ffrom%3Dlanding_page%26source%3Dphone_credit%26ho_offer_id%3D{offer_id}%26ho_trx_id%3D{transaction_id}%26affiliate_id%3D{affiliate_id}%26utm_source%3Dhasoffers%26utm_medium%3Daffiliate%26utm_campaign%3D{offer_id}%26aff_sub%3D{aff_sub}%26ref%3D{referer}\"    },    {       \"source\":\"bukalapak\",       \"name\":\"Paket Data\",       \"img\":\"image_data_package\",       \"url\":\"http://bukalapak.go2cloud.org/aff_c?offer_id=15&aff_id=4287&aff_sub=ticket&url=https%3A%2F%2Fwww.bukalapak.com%2Fpaket-data%3Fho_offer_id%3D{offer_id}%26ho_trx_id%3D{transaction_id}%26affiliate_id%3D{affiliate_id}%26utm_source%3Dhasoffers%26utm_medium%3Daffiliate%26utm_campaign%3D{offer_id}%26aff_sub%3D{aff_sub}%26ref%3D{referer}\"    },    {       \"source\":\"bukalapak\",       \"name\":\"Voucher Game\",       \"img\":\"image_game\",       \"url\":\"http://bukalapak.go2cloud.org/aff_c?offer_id=15&aff_id=4287&aff_sub=ticket&url=https%3A%2F%2Fwww.bukalapak.com%2Fvoucher-game%3Fho_offer_id%3D{offer_id}%26ho_trx_id%3D{transaction_id}%26affiliate_id%3D{affiliate_id}%26utm_source%3Dhasoffers%26utm_medium%3Daffiliate%26utm_campaign%3D{offer_id}%26aff_sub%3D{aff_sub}%26ref%3D{referer}\"    },    {       \"source\":\"bukalapak\",       \"name\":\"Tiket Kereta\",       \"img\":\"image_train\",       \"url\":\"http://bukalapak.go2cloud.org/aff_c?offer_id=15&aff_id=4287&aff_sub=ticket&url=https%3A%2F%2Fwww.bukalapak.com%2Fkereta-api%3Fho_offer_id%3D{offer_id}%26ho_trx_id%3D{transaction_id}%26affiliate_id%3D{affiliate_id}%26utm_source%3Dhasoffers%26utm_medium%3Daffiliate%26utm_campaign%3D{offer_id}%26aff_sub%3D{aff_sub}%26ref%3D{referer}\"    },    {       \"source\":\"bukalapak\",       \"name\":\"Tiket Pesawat\",       \"img\":\"image_flight\",       \"url\":\"http://bukalapak.go2cloud.org/aff_c?offer_id=15&aff_id=4287&aff_sub=ticket&url=https%3A%2F%2Fwww.bukalapak.com%2Ftiket-pesawat%3Fho_offer_id%3D{offer_id}%26ho_trx_id%3D{transaction_id}%26affiliate_id%3D{affiliate_id}%26utm_source%3Dhasoffers%26utm_medium%3Daffiliate%26utm_campaign%3D{offer_id}%26aff_sub%3D{aff_sub}%26ref%3D{referer}\"    },    {       \"source\":\"bukalapak\",       \"name\":\"Event\",       \"img\":\"image_event\",       \"url\":\"http://bukalapak.go2cloud.org/aff_c?offer_id=15&aff_id=4287&aff_sub=ticket&url=https%3A%2F%2Fwww.bukalapak.com%2Ftiket-event%3Fho_offer_id%3D{offer_id}%26ho_trx_id%3D{transaction_id}%26affiliate_id%3D{affiliate_id}%26utm_source%3Dhasoffers%26utm_medium%3Daffiliate%26utm_campaign%3D{offer_id}%26aff_sub%3D{aff_sub}%26ref%3D{referer}\"    } ]"

private val couponString = "[{\"id\":\"2019042901\",\"category\":\"coupons\",\"subcategory\":\"limited-time\",\"source\":\"bukalapak\",\"feed\":\"list\",\"name\":\"Setiap Senin & Kamis, Waktunya Nikmati Diskon Rp20.000 dari bank CIMB\",\"img\":\"https://workablehr.s3.amazonaws.com/uploads/account/logo/237647/small_logo\",\"url\":\"http://bl.id/a/eMzKWr9\",\"end\":1577707200000,\"active\":true},{\"id\":\"2019042902\",\"category\":\"coupons\",\"subcategory\":\"limited-time\",\"source\":\"bukalapak\",\"feed\":\"list\",\"name\":\"Setiap Selasa Ada Cashback dari OCBC NISP Debit (Hingga Rp75.000)\",\"img\":\"https://workablehr.s3.amazonaws.com/uploads/account/logo/237647/small_logo\",\"url\":\"http://bl.id/a/kL76shq\",\"end\":1577188800000,\"active\":true},{\"id\":\"2019042903\",\"category\":\"coupons\",\"subcategory\":\"limited-time\",\"source\":\"bukalapak\",\"feed\":\"list\",\"name\":\"Diskon Rp30.000 Setiap Selasa dengan e-Pay BRI\",\"img\":\"https://workablehr.s3.amazonaws.com/uploads/account/logo/237647/small_logo\",\"url\":\"http://bl.id/a/dTPYcth\",\"end\":1577764800000,\"active\":true},{\"id\":\"2019042904\",\"category\":\"coupons\",\"subcategory\":\"limited-time\",\"source\":\"bukalapak\",\"feed\":\"list\",\"name\":\"Hari Selasa Waktunya Belanja dengan BRI Debit Online ,dapatkan diskon Rp30.000\",\"img\":\"https://workablehr.s3.amazonaws.com/uploads/account/logo/237647/small_logo\",\"url\":\"http://bl.id/a/iP3l5jX\",\"end\":1577764800000,\"active\":true},{\"id\":\"2019042905\",\"category\":\"coupons\",\"subcategory\":\"limited-time\",\"source\":\"bukalapak\",\"feed\":\"list\",\"name\":\"Setiap Rabu Ada Diskon Rp100.000 dari Bank Mega\",\"img\":\"https://workablehr.s3.amazonaws.com/uploads/account/logo/237647/small_logo\",\"url\":\"http://bl.id/a/eB4ZXCY\",\"end\":1566964800000,\"active\":true},{\"id\":\"2019042906\",\"category\":\"coupons\",\"subcategory\":\"limited-time\",\"source\":\"bukalapak\",\"feed\":\"list\",\"name\":\"Rabu Ada Diskon Rp500.000 dengan Kredivo\",\"img\":\"https://workablehr.s3.amazonaws.com/uploads/account/logo/237647/small_logo\",\"url\":\"http://bl.id/a/m6HggvI\",\"end\":1561521600000,\"active\":true},{\"id\":\"2019042907\",\"category\":\"coupons\",\"subcategory\":\"limited-time\",\"source\":\"bukalapak\",\"feed\":\"list\",\"name\":\"Belanja Kebutuhan di Hari Kamis, Diskon Rp150.000 dari Bank UOB\",\"img\":\"https://workablehr.s3.amazonaws.com/uploads/account/logo/237647/small_logo\",\"url\":\"http://bl.id/a/p6pYMoL\",\"end\":1561608000000,\"active\":true},{\"id\":\"2019042908\",\"category\":\"coupons\",\"subcategory\":\"limited-time\",\"source\":\"bukalapak\",\"feed\":\"list\",\"name\":\"Setiap Kamis Ada Diskon Rp100.000 dari Kartu Kredit OCBC NISP\",\"img\":\"https://workablehr.s3.amazonaws.com/uploads/account/logo/237647/small_logo\",\"url\":\"http://bl.id/a/fPUsPxp\",\"end\":1564027200000,\"active\":true},{\"id\":\"2019042909\",\"category\":\"coupons\",\"subcategory\":\"limited-time\",\"source\":\"bukalapak\",\"feed\":\"list\",\"name\":\"Setiap Hari Kamis Ada Diskon Rp150.000 dengan Kartu Kredit Mandiri\",\"img\":\"https://workablehr.s3.amazonaws.com/uploads/account/logo/237647/small_logo\",\"url\":\"http://bl.id/a/nowibEa\",\"end\":1555059437000,\"active\":true},{\"id\":\"2019042910\",\"category\":\"coupons\",\"subcategory\":\"limited-time\",\"source\":\"bukalapak\",\"feed\":\"list\",\"name\":\"Paket Data Tinggal Diisi, Bisa Jalan-jalan Ke Luar Negeri\",\"img\":\"https://workablehr.s3.amazonaws.com/uploads/account/logo/237647/small_logo\",\"url\":\"http://bl.id/a/jSXJyxs\",\"end\":1559275200000,\"active\":true}]"


class Voucher(
    var url: String,
    var name: String,
    @JsonProperty("img") var image: String,
    var source: String
) : UiModel

class Coupon(

    val id: Int,
    val category: String,
    val subcategory: String,
    val source: String,
    val feed: String,
    val name: String,
    val img: String,
    val url: String,
    val end: Long,
    val active: Boolean
) : UiModel


class Deal(

    val id: Int,
    val category: String,
    val subcategory: String,
    val source: String,
    val feed: String,
    val name: String,
    val img: String,
    val url: String,
    val end: Long,
    val active: Boolean,
    val componentId: String = url.hash(),
    val batchId: String = UUID.randomUUID().toString()  // FIXIME: this is not a real batch

) : UiModel


@Repository
class VoucherRepository {

    @Inject
    lateinit var mapper: ObjectMapper

    fun getVouchers(): List<Voucher> {
        return mapper.readValue(voucherString)
    }

    fun getCoupons(): List<Coupon> {
        return mapper.readValue(couponString)
    }

    fun getDeals(): List<Deal> {
        return mapper.readValue(couponString)

    }

}