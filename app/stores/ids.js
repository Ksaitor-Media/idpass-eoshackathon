import { observable, action } from 'mobx'
import { get, post, put, patch } from 'axios'
import EOS from '../components/EOS'
import pako from 'pako'

class Ids {
  @observable loading = false
  @observable person = {}
  @observable ids = []
  @observable signedJSONLD = null

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
    this.person = {}
  }

  @action sign = () => {
    const that = this
    const data = this.person
    this.loading = true
    that.signedJSONLD = null
    post('http://localhost:3000/sign', data)
    .then(res => {
      console.log(res.data)
      that.signedJSONLD = res.data
      that.loading = false
    })

  }
}

export default new Ids()
