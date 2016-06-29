package nl.ru

import com.google.common.net.InternetDomainName
import nl.surfsara.warcutils.WarcInputFormat
import org.apache.hadoop.io.LongWritable
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.jwat.warc.{WarcReaderFactory, WarcRecord}

object CCSubdomainsApp {

    def main(args: Array[String]) {
        val conf = new SparkConf().setAppName("CCSubdomainApp").setMaster("local")
        val sc = new SparkContext(conf)

        val warcFilePath = "data/webarchive-20160629133656425-00000-6-5d875e632943.warc.gz"
        val warcFile = sc.newAPIHadoopFile(
            warcFilePath,
            classOf[WarcInputFormat],
            classOf[LongWritable],
            classOf[WarcRecord]
        )
        val warcData = warcFile.map{ wr => wr }.cache()
        val responseHeaders = warcData
            .map{wr => wr._2.header}
            .filter{_.warcTypeIdx == 2}
            .cache()
        val counts = responseHeaders
            .map{ h => getFirstSubDomain(h.warcTargetUriStr) }
            .map( subDomain => (subDomain, 1))
            .reduceByKey((c1, c2) => c1 + c2)
            .sortBy(_._2, false)
        val sizes = responseHeaders
            .map{ h => (getFirstSubDomain(h.warcTargetUriStr), h.contentLengthStr.toInt) }
            .reduceByKey((c1, c2) => c1 + c2)
            .sortBy(_._2, false)

        counts.saveAsTextFile("data/counts")
        sizes.saveAsTextFile("data/sizes")
    }

    /**
      * Extract the first sub domain of a given URL.
      *
      * @param url The URL to extract the first sub domain for.
      * @return The first sub domain.
      */
    def getFirstSubDomain(url: String) = url.split("[:]{1}[/]{2}")(1).split("[.]{1}")(0)

    /**
      * Extract the protocol from a given URL.
      *
      * @param url URL to extract the protocol for.
      * @return The protocol.
      */
    def getProtocol(url: String) = url.split("[:]{1}[/]{2}")(0)

    /**
      * Remove content type parameters.
      * For example:
      *     application/http;msgtype=response
      * Will become:
      *     application/http
      *
      * @param contentType The content type to remove parameters from.
      * @return The content type without parameters.
      */
    def removeContentTypeParameters(contentType: String): String = {
        var result = contentType
        if (contentType.contains(";")) {
            result = result.split(";")(0)
        }
        result
    }

    /**
      * Cut a string to a length of maxLength if its length is larger than maxLength.
      *
      * @param str String to examine.
      * @param maxLength Maximum length allowed.
      * @return Cutted string.
      */
    def restrictToSize(str: String, maxLength: Int): String = {
        if (str.length <= maxLength) str else str.substring(0, maxLength)
    }

}