import EOS from 'eosjs'
import ecc from 'eosjs-ecc'

const DID_CONTEXT = 'https://w3id.org/did/v1'

async function generateEOSKeyPair() {
  let privateKey = await ecc.randomKey()
  let publicKey = ecc.privateToPublic(privateKey)
  return {privateKey, publicKey}
}


function addEncodedSecp256k1PublicKey(publicKeyNode, publicKey) {
  publicKeyNode.publicKeyBase58 = publicKey
  return publicKeyNode
}


function addSecp256k1PrivateKey (privateKeyNode, privateKey) {
  privateKeyNode.privateKeyBase58 = privateKey
  return privateKeyNode
}

async function generateDid () {
  const publicDidDocument = {
    '@context': DID_CONTEXT
  }

  let did = "did:eos:" + Math.random().toString(36).substr(2, 15)

  // application suite parameters
  const appSuites = {
    authentication: {
      type: 'Secp256k1VerificationKey2018',
      publicKeyHash: 'authn-key-1'
    }
  }

  // generate a separate key pair for each app suite
  for (const name in appSuites) {
    appSuites[name].keys = await generateEOSKeyPair()
  }

  publicDidDocument.id = did

  // add app suites to DID Document
  for (const name in appSuites) {
    const appSuite = appSuites[name]
    publicDidDocument[name] = [{
      type: appSuite.type,
      publicKey: [addEncodedSecp256k1PublicKey({
        id: did + '#' + appSuite.publicKeyHash,
        type: 'Secp256k1VerificationKey2018',
        owner: did
      }, appSuite.keys.publicKey)]
    }]
  }

  // add private key information to the private DID document
  let privateDidDocument = deepClone(publicDidDocument)
  for (const name in appSuites) {
    const {privateKey} = appSuites[name].keys
    const {publicKey} = privateDidDocument[name][0]
    publicKey[0].privateKey = addSecp256k1PrivateKey({}, privateKey)
  }
  return {publicDidDocument, privateDidDocument}
}


window.generateDid = generateDid

/**
 * Clones a value. If the value is an array or an object it will be deep cloned.
 *
 * @param value the value to clone.
 *
 * @return the cloned value.
 */
function deepClone (value) {
  if (value && typeof value === 'object') {
    let rval
    if (Array.isArray(value)) {
      rval = new Array(value.length)
      for (let i = 0; i < rval.length; ++i) {
        rval[i] = deepClone(value[i])
      }
    } else {
      rval = {}
      for (let j in value) {
        rval[j] = deepClone(value[j])
      }
    }
    return rval
  }
  return value
};




let wif = '5KQwrPbwdL6PhXujxW37FSSQZ1JiwsST4cqQzDeyXtP79zkvFD3'
let pubkey = 'EOS6MRyAjQq8ud7hVNYcfnVPJqcVpscN5So8BhtHuGYqET5GDW5CV'

let eos = EOS({keyProvider: wif})

// console.log(eos)
// console.log(eos.getAccount())

window.eos = eos;
