import 'react-datepicker/dist/react-datepicker.css'
import './qrprint.css'

import React from 'react'
import { observer, inject } from 'mobx-react'
import { Container, Header, Divider, Grid } from 'semantic-ui-react'
import { Input, Form, Button, Dropdown, Image } from 'semantic-ui-react'
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
    const { person, loading, handleChange, sign, signedJSONLD, newIdentity } = this.props.IdsStore;
    const { iris, captireIris, irisLoading } = this.props.IdsStore;
    const { hardwareIDPASS } = this.props.IdsStore;
    let qr = false
    if (signedJSONLD) {
      console.log('signedJSONLD', signedJSONLD)
      qr = JSON.stringify(signedJSONLD)
    }

    return (
      <Container {...{style: {marginTop: '5em'}}}>
        <Header as='h1'>
          🤓 Identity Generator
        </Header>
        <Button link content='New Person' onClick={newIdentity} size='tiny' basic  color='green'/>
        <br />
        <br />
        <Grid columns={2}>
          <Grid.Row>
            <Grid.Column>
              <Form>
                <Form.Group>
                  <Form.Input label='Full Legal Name' name='legalName' onChange={handleChange} value={person.legalName} />
                  <Form.Input label='Short Name' name='shortName' onChange={handleChange} value={person.shortName}/>
                </Form.Group>
                <Form.Group>
                  <Form.Dropdown label='Gender' selection options={genders} name='gender' onChange={handleChange} />
                </Form.Group>
                <Form.Group>
                  <div className='field'>
                    <label>Date of Birth</label>
                    <DatePicker selected={this.state.date} name='dateOfBirth' onChange={this.handleDOBChange} />
                  </div>
                  {/*<Form.Input label='Age' name='age' onChange={handleChange}/>*/}
                </Form.Group>
              </Form>
            </Grid.Column>
            <Grid.Column>
              {(iris && iris.image) ? <Image rounded src={iris ? iris.image : 'https://react.semantic-ui.com/assets/images/wireframe/square-image.png'} /> : null}
              <br />
              <Button content='👁️ Get IRIS' onClick={captireIris} loading={irisLoading} />
            </Grid.Column>
          </Grid.Row>
        </Grid>
        <Button color='green' content='Create temporary IDPASS' onClick={sign} loading={loading} />
        <Button color='green' content='Create hardware IDPASS' onClick={hardwareIDPASS} />
        <br />
        <br />
        {qr ?  <QRCode value={qr} size={256} /> : null}
        <p>{qr ?  signedJSONLD.id : null}</p>
      </Container>
    )
  }
}

export default Home
