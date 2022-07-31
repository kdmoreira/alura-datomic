(ns alura-datomic.course-01.aula1
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [alura-datomic.ecommerce.db :as db]
            [alura-datomic.ecommerce.model :as model]))

(def conn (db/abre-conexao))

(db/cria-schema conn)

; Transact receives a connection and a sequence
(let [computador (model/novo-produto "Computador Novo",
                                     "/computador_novo",
                                     2500.1M)]
      (d/transact conn [computador]))

; Readonly db at the moment you execute this line
; if you add entities and don't execute it, you'll
; only access what was added until the last time
; the code was executed
(def db (d/db conn))

; This query retrieves all entities that have :product/nome
(d/q '[:find   ?entidade
       :where  [?entidade :produto/nome]] db)

(let [celular (model/novo-produto "Celular caro"
                                  "/celular_caro"
                                  1500.5M)]
  (d/transact conn [celular]))

; New database snapshot
(def db (d/db conn))

; Now running the query again, the new product id is returned
(d/q '[:find   ?entidade
       :where  [?entidade :produto/nome]] db)

(db/apaga-banco)