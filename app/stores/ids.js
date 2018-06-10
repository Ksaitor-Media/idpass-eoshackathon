import { observable, action } from 'mobx'
import { get, post, put, patch } from 'axios'
import EOS from '../components/EOS'
import pako from 'pako'
import moment from 'moment'

class Ids {
  @observable loading = false
  @observable person = {
    dateOfBirth: moment().subtract(22, 'years')
  }
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
    this.person = {
      dateOfBirth: moment().subtract(22, 'years')
    }
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
