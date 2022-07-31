(ns alura-datomic.course-01.aula3-4
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [alura-datomic.ecommerce.db :as db]
            [alura-datomic.ecommerce.model :as model]))

(def conn (db/abre-conexao))

(db/cria-schema conn)

; Adding products
(let [computador (model/novo-produto "Computador Novo",
                                     "/computador-novo",
                                     2500.1M)
      celular (model/novo-produto "Celular caro"
                                  "/celular-caro"
                                  1500.5M)
      calculadora {:produto/nome "Calculadora 4 operações"}
      celular-barato (model/novo-produto "Celular barato",
                                         "/celular-barato", 0.1M)]
  (d/transact conn [computador, celular, calculadora, celular-barato]))

(pprint (db/todos-os-produtos (d/db conn)))

(pprint (db/todos-os-produtos-por-slug 
         (d/db conn) "/computador-novo"))

; Unordered slugs, missing calculator since it doesn't have one
(pprint (db/todos-os-slugs (d/db conn)))

(pprint (db/todos-os-produtos-por-preco (d/db conn)))

(db/apaga-banco)