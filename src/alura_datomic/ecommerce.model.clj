(ns alura-datomic.ecommerce.model)

(defn uuid []
  (java.util.UUID/randomUUID))

(defn novo-produto 
  ([nome slug preco]
   (novo-produto (uuid) nome slug preco))
  ([uuid nome slug preco]
   {:produto/id    uuid
    :produto/nome  nome
    :produto/slug  slug
    :produto/preco preco}))