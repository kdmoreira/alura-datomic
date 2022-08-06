(ns alura-datomic.ecommerce.db
  (:require [datomic.api :as d]))

(def db-uri "datomic:dev://localhost:4334/ecommerce")

(defn abre-conexao []
  (d/create-database db-uri)
  (d/connect db-uri))

(defn apaga-banco []
  (d/delete-database db-uri))

(def schema [{:db/ident       :produto/nome
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "O nome de um produto"}
             {:db/ident       :produto/slug
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "O caminho para acessar esse produto via http"}
             {:db/ident       :produto/preco
              :db/valueType   :db.type/bigdec
              :db/cardinality :db.cardinality/one
              :db/doc         "O preço de um produto com precisão monetária"}
             {:db/ident       :produto/palavra-chave
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/many}
             {:db/ident       :produto/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity}])

(defn cria-schema [conn]
  (d/transact conn schema))

; Explicit pull field by field
;(defn todos-os-produtos [db]
;  (d/q '[:find (pull ?entidade [:produto/nome :produto/preco :produto/slug])
;         :where [?entidade :produto/nome]] db))

; Generic pull
(defn todos-os-produtos [db]
  (d/q '[:find (pull ?entidade [*])
         :where [?entidade :produto/nome]] db))

; The difference in naming slug and ?slug-a-ser-buscado
; is intentional, since slug is a clojure symbol and ?slug-a-ser...
; is the value of the datomic entity, and helps not to forget ?
; otherwise it will return an empty set
(defn todos-os-produtos-por-slug [db slug]
  (d/q '[:find  ?entidade
         :in $ ?slug-a-ser-buscado ; pass db (usually $) when using :in
         :where [?entidade :produto/slug ?slug-a-ser-buscado]]
       db slug))

; Entity commonly abbreviated as ?e or _ if you won't use it
(defn todos-os-slugs [db]
  (d/q '[:find ?qualquer-slug
         :where [_ :produto/slug ?qualquer-slug]] db))

(defn todos-os-produtos-por-preco [db]
  (d/q '[:find  ?nome, ?preco
         :keys  :produto/nome, :produto/preco
         :where [?produto :produto/preco ?preco]
                [?produto :produto/nome  ?nome]] db))

; Be careful when filtering queries, because you
; are responsible for defining a plan of action
; that is, how the database will filter the data
; in an optimized way. In this case, since you want to
; filter by price, first find all products that have a
; price and then filter them before finding all their names.
; This way, the database will only find names for products
; that have already been filtered, and not all the products.
; START BY THE MOST RESTRICTIVE FILTER!
(defn todos-os-produtos-por-preco-param [db preco-minimo]
  (d/q '[:find  ?nome, ?preco
         :in $ ?preco-buscado
         :keys  :produto/nome, :produto/preco
         :where [?produto :produto/preco ?preco] 
                [(> ?preco ?preco-buscado)]
                [?produto :produto/nome  ?nome]]
       db preco-minimo))

(defn todos-os-produtos-por-palavra-chave [db palavra-chave]
  (d/q '[:find  (pull ?entidade [*])
         :in    $ ?palavra-chave-buscada
         :where [?entidade :produto/palavra-chave ?palavra-chave-buscada]]
       db palavra-chave))

(defn um-produto-por-db-id [db db-id]
  (d/pull db '[*] db-id))

; when you don't use the db identifier, you must
; specify where it comes from, in this case, :produto/id
; these are lookup refs
(defn um-produto [db produto-id]
  (d/pull db '[*] [:produto/id produto-id]))
