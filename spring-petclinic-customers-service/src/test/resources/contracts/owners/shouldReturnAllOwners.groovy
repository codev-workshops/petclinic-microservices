package contracts.owners

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return all owners when GET /owners"
    request {
        method GET()
        url "/owners"
    }
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
            [
                id       : 1,
                firstName: "George",
                lastName : "Franklin",
                address  : "110 W. Liberty St.",
                city     : "Madison",
                telephone: "6085551023",
                pets     : [
                    [
                        id       : 1,
                        name     : "Leo",
                        birthDate: $(consumer("2010-09-07"), producer(regex("\\d{4}-\\d{2}-\\d{2}.*"))),
                        type     : [id: 1, name: "cat"]
                    ]
                ]
            ],
            [
                id       : 2,
                firstName: "Betty",
                lastName : "Davis",
                address  : "638 Cardinal Ave.",
                city     : "Sun Prairie",
                telephone: "6085551749",
                pets     : [
                    [
                        id       : 2,
                        name     : "Basil",
                        birthDate: $(consumer("2012-08-06"), producer(regex("\\d{4}-\\d{2}-\\d{2}.*"))),
                        type     : [id: 6, name: "hamster"]
                    ]
                ]
            ]
        ])
    }
}
