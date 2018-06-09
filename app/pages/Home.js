import React from 'react'
import { Link } from 'react-router-dom'
import { Container, Header, Divider } from 'semantic-ui-react'
import { Input, Form, Button, Dropdown } from 'semantic-ui-react'

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
            <Form.Input label='Date of Birth' />
          </Form.Group>
        </Form>
        <Button color='green' content='Print' />
      </Container>
    )
  }
}

export default Home
