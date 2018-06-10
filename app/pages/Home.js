import 'react-datepicker/dist/react-datepicker.css'

import React from 'react'
import { observer, inject } from 'mobx-react'
import { Container, Header, Divider } from 'semantic-ui-react'
import { Input, Form, Button, Dropdown } from 'semantic-ui-react'
import DatePicker from 'react-datepicker'
import moment from 'moment'
import EOS from '../components/EOS'
import QRCode from 'qrcode.react'
import pako from 'pako'

const genders = [{
  text: 'Male',
  value: 'male'
}, {
  text: 'Female',
  value: 'female'
}, {
  text: 'Other',
  value: 'other'
}]


@inject('IdsStore')
@observer
class Home extends React.Component {
  constructor (props) {
    super(props)
    this.state = {
      date: moment().subtract(22, 'years'),
      qr: 'nice'
    };
    this.handleDOBChange = this.handleDOBChange.bind(this);
  }

  handleDOBChange (date) {
    const { handleChange } = this.props.IdsStore
    this.setState({date})
    handleChange(null, {name:'dateOfBirth', value: date.format('MM/DD/YYYY')})
  }

  encodeForQR (object) {
    return btoa(pako.deflate(JSON.stringify(object), {to: 'string'}))
  }

  decodeFromQR (data) {
    return JSON.parse(pako.enflate(atob(data), {to: 'string'}))
  }

  render() {
    const { loading, handleChange, sign, signedJSONLD, newIdentity } = this.props.IdsStore;
    let qr = false
    if (signedJSONLD) {
      console.log('signedJSONLD', signedJSONLD)
      qr = this.encodeForQR(signedJSONLD)
    }

    return (
      <Container {...{style: {marginTop: '5em'}}}>
        <Header as='h1' content='🔑 Identity Generator' />
        <Form>
          <Form.Group>
            <Form.Input label='Full Legal Name' name='legalName' onChange={handleChange}/>
            <Form.Input label='Short Name' name='shortName' onChange={handleChange}/>
          </Form.Group>
          <Form.Group>
            <Form.Dropdown label='Gender' selection options={genders} name='gender' onChange={handleChange} />
          </Form.Group>
          <Form.Group>
            <div className='field'>
              <label>Date of Birth</label>
              <DatePicker selected={this.state.date} name='dateOfBirth' onChange={this.handleDOBChange} />
            </div>
            <Form.Input label='Age' name='age' onChange={handleChange}/>
          </Form.Group>
          {qr ?  <QRCode value={qr} size={256} /> : null}
        </Form>
        <Button color='green' content='Issue' onClick={sign} loading={loading} />
        <Button link content='New Id' onClick={newIdentity} />
      </Container>
    )
  }
}

export default Home
