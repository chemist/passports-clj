(ns passports.core
  (:require [clojure.java.io :as io]
            [clj-mmap :as mmap]
            [bytebuffer.buff :refer :all])
  (:import (org.apache.commons.compress.compressors.bzip2 BZip2CompressorInputStream)
           (java.nio ByteBuffer))
  (:gen-class))

(def passport-uri "http://www.fms.gov.ru/upload/expired-passports/list_of_expired_passports.csv.bz2")

(def base-path "base/")

(defn extract-passport [line]
  "extract passport number from 1234,123412"
  (let [result (re-find #"(^\d)(\d+),(\d+)" line)
        [all base seria number] result]
    (when (and (not (nil? base)) (not (nil? seria)) (not (nil? number)))
      {:base base :int (Integer. (str seria number))})))

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
     (with-open [out (io/output-stream (str file))]
       (doseq [x sorted]
         (.write out (pack-int x)))
       nil))))

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
              nine (io/output-stream "base/9")]
    (let [files {:0 zero
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
           (println passport)
           (.write ((keyword (str (:base passport))) files) (pack-int (:int passport))))))))
  (read->sort->write base-path))

(defn show-base [n]
  (let [buf (byte-array 4)]
   (with-open [in (io/input-stream (str base-path n))]
     (loop []
       (let [number (.read in buf)]
         (if (< number 0)
           (println "end")
           (do
             (println (str n (unpack-int buf)))
             (recur))))))))

(defn check-passport [passport]
  (let [pass (re-find #"(\d)(\d+)" passport)
        [_ file-number passport-str] pass
        passport-int (Integer. passport-str)]
    (with-open [mapped-file (mmap/get-mmap (str base-path file-number))]
        (loop [min-int-pos 0
               max-int-pos (/ (.size mapped-file) 4)]
          (let [half-int-pos (bit-shift-right (+ max-int-pos min-int-pos)  1)
                int-for-check (unpack-int (mmap/get-bytes mapped-file (* half-int-pos 4) 4))]
            (println (str "min  " min-int-pos))
            (println (str "max  " max-int-pos))
            (println (str "half " half-int-pos))
           (cond (= passport-int int-for-check) "passport found, its bad"
                 (< (- max-int-pos min-int-pos) 2) "passport was not found its good"
                 (< passport-int int-for-check) (do
                                                  (println (str passport-int " < " int-for-check))
                                                  (recur min-int-pos half-int-pos))
                 (> passport-int int-for-check) (do
                                                  (println (str passport-int " > " int-for-check))
                                                  (recur half-int-pos max-int-pos))))))))
                 
(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
