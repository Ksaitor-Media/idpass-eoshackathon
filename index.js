const express = require('express')
const app = express()
const _ = require('lodash')
const cors = require('cors')
const bodyParser = require('body-parser')

const jsonld = require('jsonld')
const jsig = require('jsonld-signatures')
jsig.use('jsonld', jsonld)

const privateKeyWif = '5KQwrPbwdL6PhXujxW37FSSQZ1JiwsST4cqQzDeyXtP79zkvFD3'
const publicKey = 'EOS6MRyAjQq8ud7hVNYcfnVPJqcVpscN5So8BhtHuGYqET5GDW5CV'

const iris = 'http://10.101.2.125:10888/iris'

let baseInput = {
  '@context': [
    'https://w3id.org/identity/v1',
    'https://w3id.org/credentials/v1',
    'http://schema.org/'
  ],
  'type': ['Person']
  // 'id': 'did:sampleDID',
  // 'name': 'Raman Shalupau',
  // 'birthDate': '06/11/1990'
}

app.use(cors())
app.use(bodyParser.json())

app.all('/sign', (req, res) => {
  let input = _.defaults(req.body, baseInput)
  jsig.sign(input, {
    algorithm: 'EcdsaKoblitzSignature2016',
    privateKeyWif: privateKeyWif,
    creator: 'https://example.com/i/alice/keys/1'
  }, (err, signedDocument) => {
    if (err) {
      console.log('Signing error:', err);
      return res.send(500);
    }
    console.log('Signed document:', signedDocument)
    return res.send(signedDocument);
  });
})

app.all('/verify', (req, res) => {
  let input = _.defaults(req.body, baseInput)

  let signedDocument = input.signedDocument

  jsig.verify(signedDocument, {
    algorithm: 'EcdsaKoblitzSignature2016',
    publicKeyWif: publicKey,
    testPublicKeyOwner: {
      "@context": jsig.SECURITY_CONTEXT_URL,
      '@id': 'https://example.com/i/alice',
      publicKey: [publicKey]
    }
  }, (err, verified) => {
    if (err) {
      return res.send('Signature verification error:' + err);
    }
    return res.send(verified);
  });
})

app.get('/', (req, res) => {
  console.log(req.query)
  res.send('hello world')
})


const port = 3000;
app.listen(port, () => {
  console.log('Example app listening on port http://localhost:' + port);
});
