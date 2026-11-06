package contracts.pets

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return all pet types when GET /petTypes"
    request {
        method GET()
        url "/petTypes"
    }
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
            [id: 1, name: "cat"],
            [id: 2, name: "dog"],
            [id: 3, name: "lizard"]
        ])
    }
}
