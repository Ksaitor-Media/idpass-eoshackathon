import React from 'react'
import { Link } from 'react-router-dom'

import { observer, inject } from 'mobx-react'
import { Container, Header, Divider } from 'semantic-ui-react'
import { Input, Form, Button, Dropdown, Loader } from 'semantic-ui-react'
import { Card, Icon, Image } from 'semantic-ui-react'
import moment from 'moment'

import EOS from '../components/EOS'
import QRCode from 'qrcode.react'
import pako from 'pako'

const decodeData = (data) => {
  JSON.parse(pako.enflate(atob(data), {to: 'string'}))
}

@inject('IdsStore')
@observer
class Reader extends React.Component {
  constructor (props) {
    super(props)
    this.state = {
      val: ''
    };
  }
  componentDidMount () {
    this.refs.scanner.focus()
  }

  scanner (el) {
    const val = el.target.value
    let decoded = null
    this.setState({val})

    try {
      decoded = JSON.parse(val)
    } catch (e) { }

    console.log(1, val)
    if (decoded) {
      this.setState(decoded)
    }
    // console.log(1, pako.enflate(atob(val), {to: 'string'}))

    // const { verify } = this.props.IdsStore
    // verify({signedDocument: val})

    // this.setState(JSON.parse(decodeData(el.target.value)))
  }


  render() {
    const person = this.state
    const length = this.state.val.length
    return (
      <Container {...{style: {marginTop: '5em'}}}>
        <Header as='h1' content='🔍 Reader' />
        {person.legalName ? null : <div><Loader active inline/> {length}...</div>}
        {person.legalName ? <Card>
          <Image src='https://react.semantic-ui.com/assets/images/avatar/large/matthew.png' />
          <Card.Content>
            <Card.Header>{person.legalName}</Card.Header>
            <Card.Meta>
              <span className='date'>{person.dateOfBirth ? moment(person.dateOfBirth).format('DD/MM/YYYY') : null}</span>
              <p>{person.shortName}</p>
            </Card.Meta>
            <Card.Description>{person.gender}</Card.Description>
          </Card.Content>
          <Card.Content extra>
            <Icon name='user' /> {person.id}
          </Card.Content>
        </Card> : null}

        {/*<QRCode value={qr} size={256} />*/}
        <input ref='scanner'
          onChange={this.scanner.bind(this)}
          onBlur={this.componentDidMount.bind(this)}
          {...{style:{opacity:0}}} />
      </Container>
    )
  }
}

export default Reader
