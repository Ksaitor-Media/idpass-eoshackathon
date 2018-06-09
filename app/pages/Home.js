import 'react-datepicker/dist/react-datepicker.css'

import React from 'react'
import { Link } from 'react-router-dom'
import { Container, Header, Divider } from 'semantic-ui-react'
import { Input, Form, Button, Dropdown } from 'semantic-ui-react'
import DatePicker from 'react-datepicker'
import moment from 'moment'
import EOS from '../components/EOS'
import QRCode from 'qrcode.react'
import pako from 'pako'
import Axios from 'axios'

const genders = [{
  text: 'Male',
  value: 'male'
}, {
  text: 'Female',
  value: 'female'
}, {
  text: 'Other',
  value: 'other'
}]

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

class Home extends React.Component {
  constructor (props) {
    super(props)
    this.state = {
      date: moment(),
      qr: 'nice'
    };
    this.handleChange = this.handleChange.bind(this);
    this.handleNameChange =
    this.handleNameChange.bind(this);
    console.log(pako.deflate(stringData, {to: 'string'}).length)
    // EOS.provisionDidDocumentOnEOS().then(console.log)
  }

  handleChange (date) {
    this.setState({date})
  }

  handleNameChange (name) {
    this.setState({qr: name.target.value})
  }

  encodeForQR (stringData) {
    return btoa(pako.deflate(stringData, {to: 'string'}))
  }

  decodeFromQR (data) {
    return JSON.parse(pako.enflate(atob(data), {to: 'string'}))
  }

  log () {

  }

  render() {
    return (
      <Container {...{style: {marginTop: '5em'}}}>
        <Header as='h1' content='🔑 Identity Generator' />
        <Form>
          <Form.Group>
            <Form.Input label='Full Legal Name' onChange={this.handleNameChange} />
            <Form.Input label='Short Name' />
          </Form.Group>
          <Form.Group>
            <Form.Dropdown label='Gender' selection options={genders} />
          </Form.Group>
          <Form.Group>
            <div className='field'>
              <label>Date of Birth</label>
              <DatePicker selected={this.state.date} onChange={this.handleChange} />
            </div>
            <Form.Input label='Age' />
          </Form.Group>
          <QRCode value={stringData} size={256} />
          —
          <QRCode value={btoa(pako.deflate(stringData, {to: 'string'}))} size={256} />
        </Form>
        <Button color='green' content='Issue' onClick={this.log.bind(this)} />
      </Container>
    )
  }
}

export default Home
