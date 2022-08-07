(ns alura-datomic.course-02.aula02
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [alura-datomic.ecommerce.db :as db]
            [alura-datomic.ecommerce.model :as model]))

(def conn (db/abre-conexao))

(db/cria-schema! conn)

; Adding products
(let [computador (model/novo-produto (model/uuid) "Computador Novo",
                                     "/computador-novo",
                                     2500.1M)
      celular (model/novo-produto (model/uuid) "Celular caro"
                                  "/celular-caro"
                                  1500.5M)
      calculadora {:produto/nome "Calculadora 4 operações"}]
  (d/transact conn [computador, celular, calculadora]))

(def celular-barato (model/novo-produto "Celular barato",
                                                       "/celular-barato", 0.1M))
(d/transact conn [celular-barato])

; Creating another celular-barato with the same id as the previous one
(def celular-barato-2 (model/novo-produto (:produto/id celular-barato)
                                          "Celular barato",
                                          "/celular-barato", 0.1M))
; This will overwrite the other entity with the same id,
; since ids are unique and they're an identity, therefore,
; they guarantee the uniqueness of the entity
; It updates the existing one
(d/transact conn [celular-barato-2])

(db/todos-os-produtos (d/db conn))

(def produtos (db/todos-os-produtos (d/db conn)))

(def primeiro-db-id (-> produtos
                        ffirst
                        :db/id))

(def primeiro-produto-id (-> produtos
                             ffirst
                             :produto/id))
primeiro-db-id
primeiro-produto-id

(db/um-produto (d/db conn) 17592186045418)
(db/um-produto-por-db-id (d/db conn) primeiro-produto-id)

(db/apaga-banco)