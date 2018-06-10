import 'semantic-ui-css/semantic.min.css'

window.log = console.log
import React from 'react'
import ReactDOM from 'react-dom'
import { Router, Route } from 'react-router'
import { Provider } from 'mobx-react'
import createBrowserHistory from 'history/createBrowserHistory'
import { RouterStore, syncHistoryWithStore as syncHistory} from 'mobx-react-router'

let routingStore = new RouterStore()
const history = syncHistory(createBrowserHistory(), routingStore)

import Header from './components/Header'
import Home from './pages/Home'
import Reader from './pages/Reader'
import Ids from './pages/Ids'

import IdsStore from './stores/ids'

ReactDOM.render((
  <Provider {...{IdsStore: IdsStore}}>
    <Router key={Math.random()} history={history}>
      <div>
        <Header />
        <Route exact path='/' component={Home} />
        <Route exact path='/reader' component={Reader} />
        <Route exact path='/ids' component={Ids} />
      </div>
    </Router>
  </Provider>
), document.getElementById('app'))
