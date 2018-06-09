import React from 'react'
import { Link } from 'react-router-dom'
import { Container, Header, Divider } from 'semantic-ui-react'
import { Input, Form, Button, Dropdown } from 'semantic-ui-react'
import moment from 'moment'

import EOS from '../components/EOS'
import QRCode from 'qrcode.react'
import pako from 'pako'

const sampleData = {
  "@context": "https://w3id.org/security/v1",
  "id": "http://example.gov/credentials/3732",
  "type": ["Credential", "ProofOfAgeCredential"],
  "issuer": "https://dmv.example.gov",
  "issued": "2010-01-01",
  "claim": {
    "id": "did:example:ebfeb1f712ebc6f1c276e12ec21",
    "ageOver": 21
  },
  "revocation": {
    "id": "http://example.gov/revocations/738",
    "type": "SimpleRevocationList2017"
  },
  "signature": {
    "type": "LinkedDataSignature2015",
    "created": "2016-06-18T21:19:10Z",
    "creator": "https://example.com/jdoe/keys/1",
    "domain": "json-ld.org",
    "nonce": "598c63d6",
    "signatureValue": "BavEll0/I1zpYw8XNi1bgVg/sCneO4Jugez8RwDg/+MCRVpjOboDoe4SxxKjkCOvKiCHGDvc4krqi6Z1n0UfqzxGfmatCuFibcC1wpsPRdW+gGsutPTLzvueMWmFhwYmfIFpbBu95t501+rSLHIEuujM/+PXr9Cky6Ed+W3JT24="
  }
}

const stringData = JSON.stringify(sampleData)

class Reader extends React.Component {
  render() {
    return (
      <Container {...{style: {marginTop: '5em'}}}>
        <Header as='h1' content='🔑 Reader' />
        <QRCode value={stringData} size={256} />
      </Container>
    )
  }
}

export default Reader
