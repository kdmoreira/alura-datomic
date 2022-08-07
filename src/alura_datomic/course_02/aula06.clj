(ns alura-datomic.course-02.aula06
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [alura-datomic.ecommerce.db :as db]
            [alura-datomic.ecommerce.model :as model]))

(def conn (db/abre-conexao))

(db/cria-schema! conn)

(def eletronicos (model/nova-categoria "Eletrônicos"))
(def esporte (model/nova-categoria "Esporte"))

(pprint @(db/adiciona-categorias! conn [eletronicos, esporte]))

(def categorias (db/todas-as-categorias (d/db conn)))
categorias

(def computador (model/novo-produto (model/uuid) "Computador Novo", "/computador-novo", 2500.10M))
(def celular (model/novo-produto (model/uuid) "Celular Caro", "/celular", 888888.10M))
(def calculadora {:produto/nome "Calculadora com 4 operações"})
(def celular-barato (model/novo-produto "Celular Barato", "/celular-barato", 0.1M))
(def xadrez (model/novo-produto "Tabuleiro de xadrez", "/tabuleiro-de-xadrez", 30M))
(pprint @(db/adiciona-produtos! conn [computador, celular, calculadora, celular-barato, xadrez] 
                                "200.216.222.125"))

(db/atribui-categorias! conn [computador, celular, celular-barato] eletronicos)
(db/atribui-categorias! conn [xadrez] esporte)

; Inserting entity with nested category map (category will be added together)
(db/adiciona-produtos! conn [{:produto/nome     "Camiseta"
                              :produto/slug      "/camiseta"
                              :produto/preco     30M
                              :produto/id        (model/uuid)
                              :produto/categoria {:categoria/nome "Roupas"
                                                  :categoria/id   (model/uuid)}}])

; Inserting entity with category that already exists: use a lookup ref
(db/adiciona-produtos! conn [{:produto/nome     "Dama"
                              :produto/slug      "/dama"
                              :produto/preco     15M
                              :produto/id        (model/uuid)
                              :produto/categoria [:categoria/id (:categoria/id esporte)]}])

(def produtos (db/todos-os-produtos (d/db conn)))
(pprint produtos)

; Nested queries
(db/todos-os-produtos-mais-caros-v1 (d/db conn))
(db/todos-os-produtos-mais-caros-v2 (d/db conn))

; Using a transaction attribute to look for products
(db/todos-os-produtos-do-ip (d/db conn) "200.216.222.125")

(db/apaga-banco)