(ns alura-datomic.course-01.aula2
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [alura-datomic.ecommerce.db :as db]
            [alura-datomic.ecommerce.model :as model]))

(def conn (db/abre-conexao))

(db/cria-schema! conn)

; Not all identifiers were passed, but Datomic supports it
(let [calculadora {:produto/nome "Calculadora 4 operações"}]
  (d/transact conn [calculadora]))

; This would raise an error: Error: nil is not a legal value
;(let [radio-relogio {:produto/nome "Rádio com relógio"
;                     :produto/slug nil}] 
;  (d/transact conn [radio-relogio]))

; Adding a new phone
(let [celular-barato (model/novo-produto "Celular barato",
                                         "/celular-barato", 888M)]
  (d/transact conn [celular-barato]))

; Changing the previous phone price
; using @ for (d/transact...) since this function returns
; a promise/future
(let [celular-barato (model/novo-produto "Celular barato",
                                         "/celular-barato", 888M)
      entity-id (-> @(d/transact conn [celular-barato])
                    :tempids vals first)] ; returns the id
  (d/transact conn [[:db/add entity-id :produto/preco 0.1M]]) ; Updates the entity
  (d/transact conn [[:db/retract entity-id
                     :produto/slug "/celular-barato"]])) ; Removes slug

(db/apaga-banco)