import React, { useContext } from 'react';
import { View, StyleSheet, StatusBar, Text, SafeAreaView } from 'react-native';
import { Button, ButtonContainer } from '../components/Button';
import { Alert } from '../components/Alert';
import database from '../database/database';
import { AuthContext } from '../context/AuthContext';

class Quiz extends React.Component {
  state = {
    correctCount: 0,
    totalCount: this.props.route.params.questions.length,
    activeQuestionIndex: 0,
    answered: false,
    answerCorrect: false
  };

  answer = correct => {
    this.setState(
      state => {
        const nextState = { answered: true };

        if (correct) {
          nextState.correctCount = state.correctCount + 1;
          nextState.answerCorrect = true;
        } else {
          nextState.answerCorrect = false;
        }

        return nextState;
      },
      () => {
        setTimeout(() => this.nextQuestion(), 750);
      }
    );
  };

  nextQuestion = async () => {
    this.setState(state => {
      const nextIndex = state.activeQuestionIndex + 1;

      if (nextIndex >= state.totalCount) {
        const { user } = this.props.context;
        const { quizId } = this.props.route.params;
        
        database.addResult({
          quizId,
          studentId: user.id,
          score: state.correctCount,
          totalQuestions: state.totalCount
        });
        
        this.props.navigation.navigate('QuizResult', {
          correctCount: state.correctCount,
          totalCount: state.totalCount,
          quizId
        });
        return;
      }

      return {
        activeQuestionIndex: nextIndex,
        answered: false
      };
    });
  };

  render() {
    const questions = this.props.route.params.questions;
    const question = questions[this.state.activeQuestionIndex];

    return (
      <View
        style={[
          styles.container,
          { backgroundColor: this.props.route.params.color || "#36B1F0" }
        ]}
      >
        <StatusBar barStyle="light-content" />
        <SafeAreaView style={styles.safearea}>
          <View>
            <Text style={styles.text}>{question.text}</Text>

            <ButtonContainer>
              {question.answers.map(answer => (
                <Button
                  key={answer.id}
                  text={answer.text}
                  onPress={() => this.answer(answer.correct)}
                />
              ))}
            </ButtonContainer>
          </View>

          <Text style={styles.text}>
            {`${this.state.correctCount}/${this.state.totalCount}`}
          </Text>
        </SafeAreaView>
        <Alert
          correct={this.state.answerCorrect}
          visible={this.state.answered}
        />
      </View>
    );
  }
}

const QuizWrapper = (props) => {
  const context = useContext(AuthContext);
  return <Quiz {...props} context={context} />;
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: "#36B1F0",
    flex: 1,
    paddingHorizontal: 20
  },
  text: {
    color: "#fff",
    fontSize: 25,
    textAlign: "center",
    letterSpacing: -0.02,
    fontWeight: "600"
  },
  safearea: {
    flex: 1,
    marginTop: 100,
    justifyContent: "space-between"
  }
});

export default QuizWrapper;