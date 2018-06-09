import React from 'react'
import { Link } from 'react-router-dom'
import { Input, Button, Container, Grid } from 'semantic-ui-react'

class Home extends React.Component {
  render() {
    return (<Container>
        <h1>ID PASS!</h1>
        <Input label='Name' />
        <br/>
        <br/>
        <Button primary content='done' />
      </Container>
    )
  }
}

export default Home
