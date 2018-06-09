import 'react-datepicker/dist/react-datepicker.css'

import React from 'react'
import { Link } from 'react-router-dom'
import { Container, Header, Divider } from 'semantic-ui-react'
import { Input, Form, Button, Dropdown } from 'semantic-ui-react'
import DatePicker from 'react-datepicker'
import moment from 'moment'
import EOS from '../components/EOS'

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

class Home extends React.Component {
  constructor (props) {
    super(props)
    this.state = {
      date: moment()
    };
    this.handleChange = this.handleChange.bind(this);
  }

  handleChange (date) {
    this.setState({date})
  }

  render() {
    return (
      <Container {...{style: {marginTop: '5em'}}}>
        <Header as='h1' content='🔑 ID PASS' />
        <Form>
          <Form.Group>
            <Form.Input label='Full Legal Name' />
            <Form.Input label='Short Name' />
          </Form.Group>
          <Form.Group>
            <Form.Dropdown label='Gender' selection options={genders} />
          </Form.Group>
          <Form.Group>
            <div className='field'>
              <label>Date of Birth</label>
              <DatePicker selected={this.state.date} onChange={this.handleChange} />
            </div>
            <Form.Input label='Age' />
          </Form.Group>
        </Form>
        <Button color='green' content='Issue' />
      </Container>
    )
  }
}

export default Home
