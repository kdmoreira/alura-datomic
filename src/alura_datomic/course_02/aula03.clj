(ns alura-datomic.course-02.aula03
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [alura-datomic.ecommerce.db :as db]
            [alura-datomic.ecommerce.model :as model]))

(def conn (db/abre-conexao))

(db/cria-schema! conn)

(def eletronicos (model/nova-categoria "Eletrônicos"))
(def esporte (model/nova-categoria "Esporte"))

(pprint @(d/transact conn [eletronicos, esporte]))

(def categorias (db/todas-as-categorias (d/db conn)))
categorias

; Adding products
(let [computador (model/novo-produto (model/uuid) "Computador Novo",
                                     "/computador-novo",
                                     2500.1M)
      celular-caro (model/novo-produto (model/uuid) "Celular caro"
                                       "/celular-caro"
                                       1500.5M)
      calculadora {:produto/nome "Calculadora 4 operações"}
      celular-barato (model/novo-produto "Celular barato",
                                         "/celular-barato", 0.1M)
      xadrez (model/novo-produto "Tabuleiro de xadrez",
                                 "/tabuleiro-de-xadrez", 30M)]
  (d/transact conn [computador, celular-caro, calculadora, celular-barato, xadrez]))

(db/todos-os-produtos (d/db conn))




(def produtos (db/todos-os-produtos (d/db conn)))
produtos

(def id-computador (-> produtos
                       ffirst
                       :produto/id))
id-computador

(db/um-produto (d/db conn) id-computador)

(def id-celular-caro (-> produtos
                         second
                         first
                         :produto/id))
id-celular-caro

(def id-xadrez (-> produtos
                   last
                   first
                   :produto/id))
id-xadrez

(def id-eletronicos (-> categorias
                        ffirst
                        :categoria/id))
id-eletronicos

(def id-esportes (-> categorias
                     second
                     first
                     :categoria/id))
id-esportes

; Adding a category to a product
(d/transact conn [[:db/add [:produto/id id-computador]
                   :produto/categoria [:categoria/id id-eletronicos]]])
(d/transact conn [[:db/add [:produto/id id-celular-caro]
                   :produto/categoria [:categoria/id id-eletronicos]]])
(d/transact conn [[:db/add [:produto/id id-xadrez]
                   :produto/categoria [:categoria/id id-esportes]]])

(db/apaga-banco)