import { observable, action } from 'mobx'
import { get, post, put, patch } from 'axios'
import EOS from '../components/EOS'

class Ids {
  @observable loading = false
  @observable ids = []

  @action generateDID = () => {
    this.loading = true
    let ids = this.ids
    EOS.provisionDidDocumentOnEOS().then((data) => {
      ids.push(data)
      this.loading = false
    })
  }

  @action sign = () => {

  }
}

export default new Ids()
