import { observable, action } from 'mobx'
import { get, post, put, patch } from 'axios'

class Ids {
  @observable loading = false
  @observable ids = []

  @action generateDID = () => {
    this.loading = true
    this.loading = false
  }

  @action sign = () => {

  }
}

export default Ids
