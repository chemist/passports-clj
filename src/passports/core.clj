(ns passports.core
  (:require [clojure.java.io :as io]
            [bytebuffer.buff :refer :all])
  (:import (org.apache.commons.compress.compressors.bzip2 BZip2CompressorInputStream)
           (java.nio ByteBuffer))
  (:gen-class))

(def passport-uri "http://www.fms.gov.ru/upload/expired-passports/list_of_expired_passports.csv.bz2")

(defn extract-passport [line]
  "extract passport number from 1234,123412"
  (let [result (re-find #"(\d)(\d+),(\d+)" line)
        [all base seria number] result]
    (when (and (not (nil? base)) (not (nil? seria)) (not (nil? number)))
      {:base base :int (Integer. (str seria number))})))

(defn prepare-base-from-uri [uri]
  "download bzip file, unpack, parse, write to base/{N}, sort every file"
   (with-open [in (io/input-stream uri)
               unpack (BZip2CompressorInputStream. in)
               as-reader (io/reader unpack)
               zero (io/output-stream "base/0")
               one (io/output-stream "base/1")
               two (io/output-stream "base/2")
               three (io/output-stream "base/3")
               four (io/output-stream "base/4")
               five (io/output-stream "base/5")
               six (io/output-stream "base/6")
               seven (io/output-stream "base/7")
               eight (io/output-stream "base/8")
               nine (io/output-stream "base/9")
               files {:0 zero
                      :1 one
                      :2 two
                      :3 three
                      :4 four
                      :5 five
                      :6 six
                      :7 seven
                      :8 eight
                      :9 nine}]
       (doseq [line (line-seq as-reader)]
        (let [passport (extract-passport line)]
          (when (not (nil? passport))
            (.write ((keyword (str (:base passport))) files) (:int passport))))))
  (read->sort->write base-path))

(defn pack-int [data]
  (let [bb (ByteBuffer/allocate 4)
        buf (byte-array 4)]
    (doto bb
      (.putInt (.intValue data))
      (.flip)
      (.get buf))
    buf))

(defn unpack-int [buf]
  (let [bb (ByteBuffer/allocate 4)]
    (.put bb buf 0 4)
    (.flip bb)
    (.getInt bb)))

(defn read->sort [file]
  (with-open [zero (io/input-stream file)]
    (let [buf (byte-array 4)]
      (loop [base []
             counter 0]
        (let [readed (.read zero buf)
              body (unpack-int buf)]
          (if (> readed 0)
            (recur (conj base body) (inc counter))
            (sort base)))))))

(defn read->sort->write [base-path]
  (for [x (range 10)]
   (let [file (str base-path x)
         sorted (read->sort file)]
     (with-open [out (io/output-stream (str file "-sorted"))]
       (doseq [x sorted]
         (.write out (pack-int x)))
       nil))))

(def base-path "base/")

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
