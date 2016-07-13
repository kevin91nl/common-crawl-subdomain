package nl.ru

import java.text.SimpleDateFormat
import java.util.Calendar

import org.jsoup.Jsoup
import com.google.common.net.InternetDomainName
import nl.surfsara.warcutils.WarcInputFormat
import org.apache.hadoop.io.LongWritable
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.jwat.warc.{WarcReaderFactory, WarcRecord}
import org.apache.commons.lang.StringUtils

object CCSubdomainsApp {

    val run_local = true

    def main(args: Array[String]) {
        val conf = new SparkConf()
            .setAppName("CCSubdomainApp")
        if (run_local) {
            // Simulate locally multiple threads
            conf.setMaster("local[4]")
        }
        val sc = new SparkContext(conf)

        // Set a prefix for the output folder
        val format = new SimpleDateFormat("y-M-d_H-m-s")
        val output_prefix = "-" + format.format(Calendar.getInstance().getTime())

        // Can be comma seperated to go over multiple files
        val warcFilePath = if (run_local) "data/archive.warc.gz" else "hdfs:///data/public/common-crawl/crawl-data/CC-MAIN-2016-07/segments/1454701165302.57/warc/CC-MAIN-20160205193925-00246-ip-10-236-182-209.ec2.internal.warc.gz"
        val warcFile = sc.newAPIHadoopFile(
            warcFilePath,
            classOf[WarcInputFormat],
            classOf[LongWritable],
            classOf[WarcRecord]
        )

        // Now retrieve all texts from HTML responses
        warcFile
            .filter{ _._2.header.warcTypeIdx == 2 /* response */ }
            .filter{ _._2.getHttpHeader() != null /* make sure the header exists */ }
            .filter{ _._2.getHttpHeader().contentType != null /* make sure the content type exists */ }
            .filter{ _._2.getHttpHeader().contentType.startsWith("text/html") /* fetch only HTML pages */ }
            .map{wr => (getFirstSubDomain(wr._2.header.warcTargetUriStr), 1)}
            .cache() /* Question: do you think this cache will help further processing? */
            .reduceByKey((a, b) => a + b)
            .sortBy(_._2, false)
            //.saveAsTextFile("data/result" + output_prefix)
            .foreach(println)
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
      * application/http;msgtype=response
      * Will become:
      * application/http
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
      * @param str       String to examine.
      * @param maxLength Maximum length allowed.
      * @return Cutted string.
      */
    def restrictToSize(str: String, maxLength: Int): String = {
        if (str.length <= maxLength) str else str.substring(0, maxLength)
    }

}
