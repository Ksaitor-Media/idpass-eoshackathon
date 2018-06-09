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

import Home from './pages/Home'

ReactDOM.render((
  <Router key={Math.random()} history={history}>
    <div>
      <Route exact path='/' component={Home} />
    </div>
  </Router>
), document.getElementById('app'))
