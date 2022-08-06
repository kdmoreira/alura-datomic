(ns alura-datomic.course-01.aula6
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

; Brings products that cost more than 1000M
(pprint (db/todos-os-produtos-por-preco-param
         (d/db conn) 1000))

(db/todos-os-produtos (d/db conn))

; Adds two tags for product
(d/transact conn [[:db/add 17592186045418 :produto/palavra-chave "desktop"]
                   [:db/add 17592186045418 :produto/palavra-chave "computador"]])

(db/todos-os-produtos (d/db conn))

; Removes one of the tags
(d/transact conn [[:db/retract 17592186045418 :produto/palavra-chave "computador"]])

(db/todos-os-produtos-por-palavra-chave (d/db conn) "desktop")

(db/apaga-banco)
