import React from 'react'
import { Link } from 'react-router-dom'
import { Container, Header, Divider } from 'semantic-ui-react'
import { Input, Form, Button, Dropdown } from 'semantic-ui-react'
import moment from 'moment'

import EOS from '../components/EOS'
import QRCode from 'qrcode.react'
import pako from 'pako'

const decodeData = (binaryString) => {
  return JSON.parse(pako.inflate(binaryString, { to: 'string' }));
}

class Reader extends React.Component {
  constructor (props) {
    super(props)
    this.state = {
      qr: ''
    };
  }
  componentDidMount () {
    this.refs.scanner.focus()
  }

  scanner (el) {
    console.log(el.target.value)
    this.setState({qr: el.target.value})
    // this.setState(JSON.parse(decodeData(el.taarget.value)))
  }


  render() {
    const { qr, } = this.state
    return (
      <Container {...{style: {marginTop: '5em'}}}>
        <input ref='scanner'
          onChange={this.scanner.bind(this)}
          onBlur={this.componentDidMount.bind(this)}
          {...{style:{opacity:0}}} />

        <Header as='h1' content='🤓 Reader' />
        <p>{this.state.qr}</p>
        {this.state.fullLegalName}
        <QRCode value={qr} size={256} />
      </Container>
    )
  }
}

export default Reader
