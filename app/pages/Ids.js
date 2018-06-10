import React from 'react'
import { observer, inject } from 'mobx-react'
import { Container, Header, Divider, Button} from 'semantic-ui-react'
import EOS from '../components/EOS'


@inject('IdsStore')
@observer
class Ids extends React.Component {
  generateId () {
    this.props.IdsStore.generateDID()
  }

  render() {
    const { loading, ids } = this.props.IdsStore
    const qty = ids.length || 0
    return (
      <Container {...{style: {marginTop: '5em'}}}>
        <Header as='h1' content='🔑 Identities' />
        <Button
          color='green' content='Issue new ID'
          onClick={this.generateId.bind(this)} />
        <Header as='h3' content={`${qty} keys`} />
        <ul>
        {ids.map(id => {
          return <li key={id.publicDidDocument.id}>{id.publicDidDocument.id}</li>
        })}
        </ul>
      </Container>
    )
  }
}

export default Ids
