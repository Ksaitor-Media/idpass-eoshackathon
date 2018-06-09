import React from 'react'
import { Link } from 'react-router-dom'
import { Container, Header, Divider, Button} from 'semantic-ui-react'
import EOS from '../components/EOS'

class Ids extends React.Component {
  constructor (props) {
    super(props)
    this.state = {
      ids: []
    };
  }

  generateId () {
    let that = this
    EOS.provisionDidDocumentOnEOS().then((data) => {
      console.log(data)
      let ids = that.state.ids
      ids.push(data)
      that.setState({ids})
    })
  }

  render() {
    const { ids } = this.state
    console.log(ids)
    return (
      <Container {...{style: {marginTop: '5em'}}}>
        <Header as='h1' content='🔑 Identities' />
        <Button color='green' content='Issue an id' onClick={this.generateId.bind(this)}/>
        <ul>
        {ids.map(id => {
          return <li>{id.publicDidDocument.id}</li>
        })}
        </ul>
      </Container>
    )
  }
}

export default Ids
