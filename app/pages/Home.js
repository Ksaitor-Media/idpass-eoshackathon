import React from 'react'
import { Link } from 'react-router-dom'
import { Input, Form, Button, Container, Header } from 'semantic-ui-react'

class Home extends React.Component {
  render() {
    return (
      <Container>
        <Header as='h1' content='ID PASS' />
        <Form>
          <Form.Group>
            <Form.Input label='Name' />
          </Form.Group>
        </Form>
        <Button color='green' content='Save' />
      </Container>
    )
  }
}

export default Home
