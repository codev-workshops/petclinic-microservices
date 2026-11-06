package contracts.chat

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return the assistant's plain-text answer when POST /chatclient"
    request {
        method POST()
        url "/chatclient"
        headers {
            contentType applicationJson()
        }
        body('"Which vets do you have?"')
    }
    response {
        status OK()
        body("We have James Carter and Helen Leary.")
    }
}
