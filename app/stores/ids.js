import { observable, action } from 'mobx'
import { get, post, put, patch } from 'axios'
import EOS from '../components/EOS'
import pako from 'pako'
import moment from 'moment'

class Ids {
  @observable loading = false
  @observable irisLoading = false
  @observable person = {
    legalName: '',
    dateOfBirth: moment().subtract(22, 'years')
  }
  @observable ids = []
  @observable signedJSONLD = null
  @observable iris = null

  @action generateDID = () => {
    this.loading = true
    let ids = this.ids
    EOS.provisionDidDocumentOnEOS().then((data) => {
      ids.push(data)
      this.loading = false
    })
  }

  @action handleChange = (e, { name, value, checked }) => {
    console.log('handleChange', name, value)
    this.person[name] = value
  }

  @action newIdentity = () => {
    this.person = {
      legalName: '',
      dateOfBirth: moment().subtract(22, 'years')
    }
    this.signedJSONLD = null
  }

  @action sign = () => {
    const that = this
    let data = this.person
    this.loading = true
    that.signedJSONLD = null
    const did = this.ids.pop()
    data.id = did.publicDidDocument.id

    post('http://localhost:3000/sign', data)
    .then(res => {
      console.log(res.data)
      that.signedJSONLD = res.data
      that.loading = false
    })

  }

  @action captireIris = () => {
    console.log('captireIris')
    const that = this
    const iris = 'http://10.101.2.125:10888/capture_iris'
    that.irisLoading = true
    get(iris).then(res => {
      console.log(res.data)
      that.iris = res.data
      that.irisLoading = false
    })
  }


  @action verify = (data) => {
    const that = this
    this.loading = true
    post('http://localhost:3000/verify', data)
    .then(res => {
      console.log(res)
      that.valid = res
      that.loading = false
    })

  }
}

export default new Ids()
