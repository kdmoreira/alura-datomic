(ns alura-datomic.course-01.aula5
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [alura-datomic.ecommerce.db :as db]
            [alura-datomic.ecommerce.model :as model]))

(def conn (db/abre-conexao))

(db/cria-schema! conn)

; Adding products and using result to see what is :db-after
(let [computador (model/novo-produto "Computador Novo",
                                     "/computador-novo",
                                     2500.1M)
      celular (model/novo-produto "Celular caro"
                                  "/celular-caro"
                                  1500.5M)
      resultado @(d/transact conn [computador, celular])]
  (pprint resultado))

; We can use the present instant to take a snapshot like this
(def snapshot-of-the-past (d/db conn))

(let [calculadora {:produto/nome "Calculadora 4 operações"}
      celular-barato (model/novo-produto "Celular barato",
                                         "/celular-barato", 0.1M)]
  (d/transact conn [calculadora, celular-barato]))

; Snapshot in the d/db instant = 4 products
(pprint (count (db/todos-os-produtos (d/db conn))))

; Now the query reflects a moment in the past = 2 products
(pprint (count (db/todos-os-produtos snapshot-of-the-past)))

; Snapshot in the d/db filtered with data from the past
; using an instant and the filter asOf
; Snapshot that will return 2 products
(pprint (db/todos-os-produtos 
         (d/as-of (d/db conn) 
                     #inst "2022-07-23T19:14:29.099-00:00")))

; Snapshot of the data after, returns 4 products
(pprint (db/todos-os-produtos
         (d/as-of (d/db conn)
                  #inst "2022-07-23T19:14:31.747-00:00")))

(db/apaga-banco)