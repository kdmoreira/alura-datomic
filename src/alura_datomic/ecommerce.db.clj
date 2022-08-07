(ns alura-datomic.ecommerce.db
  (:require [datomic.api :as d]))

(def db-uri "datomic:dev://localhost:4334/ecommerce")

(defn abre-conexao []
  (d/create-database db-uri)
  (d/connect db-uri))

(defn apaga-banco []
  (d/delete-database db-uri))

(def schema [; Produtos
             {:db/ident       :produto/nome
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
              :db/unique      :db.unique/identity}
             {:db/ident       :produto/categoria
              :db/valueType   :db.type/ref
              :db/cardinality :db.cardinality/one}

             ; Categorias
             {:db/ident       :categoria/nome
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one}
             {:db/ident       :categoria/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity}
             
             ; Transações
             {:db/ident       :tx-data/ip
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one}])

(defn cria-schema! [conn]
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

(defn todas-as-categorias [db]
  (d/q '[:find (pull ?categoria [*])
         :where [?categoria :categoria/id]]
       db))

(defn db-adds-de-atribuicao-de-categoria
  [produtos categoria]
  (reduce (fn [db-adds produto] (conj db-adds
                                      [:db/add
                                       [:produto/id (:produto/id produto)]
                                       :produto/categoria
                                       [:categoria/id (:categoria/id categoria)]]))
          []
          produtos))

(defn atribui-categorias! [conn produtos categoria]
  (let [a-transacionar (db-adds-de-atribuicao-de-categoria produtos categoria)]
    (d/transact conn a-transacionar)))

(defn adiciona-produtos!
  ([conn produtos]
   (d/transact conn produtos))
  ([conn produtos ip]
   (let [db-add-ip [:db/add "datomic.tx" :tx-data/ip ip]]
     (d/transact conn (conj produtos db-add-ip)))))

(defn adiciona-categorias! [conn categorias]
  (d/transact conn categorias))

(defn todos-os-nomes-de-produtos-e-categorias [db]
  (d/q '[:find ?nome ?nome-da-categoria
         :keys produto categoria
         :where [?produto :produto/nome ?nome]
         [?produto :produto/categoria ?categoria]
         [?categoria :categoria/nome ?nome-da-categoria]]
       db))

; Using FORWARD navigation in the pull to bring category info as well
; "who is produto referencing?", notice the {}
(defn todos-os-produtos-da-categoria-fw [db nome-da-categoria]
  (d/q '[:find (pull ?produto [* {:produto/categoria [:categoria/nome]}])
         :in $ ?nome
         :where [?categoria :categoria/nome ?nome]
         [?produto :produto/categoria ?categoria]]
       db nome-da-categoria))

; Using BACKWARD navigation in the pull to bring products that reference the category
; Notice the underscore
(defn todos-os-produtos-da-categoria-bw [db nome-da-categoria]
  (d/q '[:find (pull ?categoria [:categoria/nome {:produto/_categoria [:produto/nome
                                                                       :produto/slug]}])
         :in $ ?nome
         :where [?categoria :categoria/nome ?nome]
         [?produto :produto/categoria ?categoria]]
       db nome-da-categoria))

; Be careful because datomic works with sets, therefore, if you want to know how many
; products that have a price are there in the database, and use a (count ?preco)
; datomic will treat prices as a set and will not count repeating ones, that is,
; if there are 6 products in total and two have the same price, it will count 5
; Therefore, use :with and inform an unique info about the entity so that it
; considers it when executing :find...
(defn resumo-dos-produtos [db]
  (d/q '[:find (min ?preco) (max ?preco) (count ?preco) (sum ?preco)
         :keys minimo maximo quantidade preco-total
         :with ?produto
         :where [?produto :produto/preco ?preco]]
       db))

(defn resumo-dos-produtos-por-categoria [db]
  (d/q '[:find ?categoria (min ?preco) (max ?preco) (count ?preco) (sum ?preco)
         :keys categoria minimo maximo quantidade preco-total
         :with ?produto
         :where [?produto :produto/preco ?preco]
         [?produto :produto/categoria ?categoria]
         [?categoria :categoria/nome ?nome]]
       db))

; Variation that executes two separate queries
(defn todos-os-produtos-mais-caros-v1 [db]
  (let [preco-mais-alto (ffirst (d/q '[:find (max ?preco)
                                       :where [_ :produto/preco ?preco]]
                                     db))]
    (d/q '[:find (pull ?produto [*])
           :in $ ?preco
           :where [?produto :produto/preco ?preco]]
         db preco-mais-alto)))

; Variation that executes a nested query
(defn todos-os-produtos-mais-caros-v2 [db]
  (d/q '[:find (pull ?produto [*])
         :where [(q '[:find (max ?preco)
                      :where [_ :produto/preco ?preco]]
                    $) [[?preco]]] ; the input ($ -> the db itself) and the output (binded as ?preco) 
                [?produto :produto/preco ?preco]]
       db))

; Adding transaction info
(defn todos-os-produtos-do-ip [db ip]
  (d/q '[:find (pull ?produto [*])
         :in $ ?ip-buscado
         :where [?transacao :tx-data/ip ?ip-buscado]
                [?produto :produto/id _ ?transacao]]
       db ip))