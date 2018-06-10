import React from 'react'
import { observer, inject } from 'mobx-react'
import { Link } from 'react-router-dom'
import { Container, Button } from 'semantic-ui-react'

@inject('IdsStore')
@observer
class Header extends React.Component {
  render() {
    const { ids } = this.props.IdsStore;
    const qty = ids.length || 0
    return (
      <Container text {...{style: {marginTop: '1vh'}}} textAlign='center'>
        <Button.Group basic>
          <Button as={Link} to='/' content={qty +' IDs'} />
          <Button as={Link} to='/register' content='Register' />
          <Button as={Link} to='/reader' content='Reader' />
        </Button.Group>
      </Container>
    )
  }
}

export default Header
