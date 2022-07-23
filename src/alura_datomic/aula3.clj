(ns alura-datomic.aula3
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [alura-datomic.ecommerce.db :as db]
            [alura-datomic.ecommerce.model :as model]))

(def conn (db/abre-conexao))

(db/cria-schema conn)

; Adding products
(let [computador (model/novo-produto "Computador Novo",
                                     "/computador_novo",
                                     2500.1M)
      celular (model/novo-produto "Celular caro"
                                  "/celular_caro"
                                  1500.5M)
      calculadora {:produto/nome "Calculadora 4 operações"}
      celular-barato (model/novo-produto "Celular barato",
                                         "/celular-barato", 0.1M)]
  (d/transact conn [computador, celular, calculadora, celular-barato]))

(pprint (db/todos-os-produtos (d/db conn)))

(db/apaga-banco)