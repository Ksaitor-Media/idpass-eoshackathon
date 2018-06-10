import React from 'react'
import { Link } from 'react-router-dom'
import { Container, Button } from 'semantic-ui-react'

class Header extends React.Component {
  render() {
    return (
      <Container text {...{style: {marginTop: '1vh'}}} textAlign='center'>
        <Button.Group basic>
          <Button as={Link} to='/ids' content='IDs' />
          <Button as={Link} to='/' content='Register' />
          <Button as={Link} to='/reader' content='Reader' />
        </Button.Group>
      </Container>
    )
  }
}

export default Header
