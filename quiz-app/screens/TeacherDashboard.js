// src/screens/TeacherDashboard.js
import React, { useState, useEffect, useContext } from 'react';
import { View, Text, TouchableOpacity, FlatList, StyleSheet, Alert } from 'react-native';
import database from '../database/database';
import { AuthContext } from '../context/AuthContext';

const TeacherDashboard = ({ navigation }) => {
  const [quizzes, setQuizzes] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const { user, logout } = useContext(AuthContext);

  useEffect(() => {
    const loadQuizzes = async () => {
      if (user) {
        setIsLoading(true);
        try {
          const teacherQuizzes = await database.getQuizzesByTeacher(user.id);
          setQuizzes(teacherQuizzes);
        } catch (error) {
          Alert.alert('Error', 'Failed to load quizzes');
          console.error(error);
        } finally {
          setIsLoading(false);
        }
      }
    };
    loadQuizzes();
  }, [user]);

  const handleCreateQuiz = () => {
    navigation.navigate('CreateQuiz');
  };

  const handleEditQuiz = (quiz) => {
    navigation.navigate('EditQuiz', { quizId: quiz.id });
  };

  const handlePublishQuiz = async (quizId) => {
    try {
      const quiz = await database.getQuizById(quizId);
      await database.updateQuiz({
        ...quiz,
        published: true
      });
      const updatedQuizzes = await database.getQuizzesByTeacher(user.id);
      setQuizzes(updatedQuizzes);
      Alert.alert('Success', 'Quiz published successfully');
    } catch (error) {
      Alert.alert('Error', 'Failed to publish quiz');
      console.error(error);
    }
  };

  const handleDeleteQuiz = async (quizId) => {
    Alert.alert(
      'Confirm Delete',
      'Are you sure you want to delete this quiz?',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Delete',
          onPress: async () => {
            try {
              await database.deleteQuiz(quizId);
              const updatedQuizzes = await database.getQuizzesByTeacher(user.id);
              setQuizzes(updatedQuizzes);
            } catch (error) {
              Alert.alert('Error', 'Failed to delete quiz');
              console.error(error);
            }
          },
        },
      ]
    );
  };

  const handleLogout = () => {
    logout();
    navigation.navigate('Login');
  };

  React.useLayoutEffect(() => {
    navigation.setOptions({
      headerRight: () => (
        <TouchableOpacity onPress={handleLogout} style={styles.logoutButton}>
          <Text style={styles.logoutText}>Logout</Text>
        </TouchableOpacity>
      ),
    });
  }, [navigation]);

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Teacher Dashboard</Text>
      
      <TouchableOpacity 
        style={styles.createButton} 
        onPress={handleCreateQuiz}
        disabled={isLoading}
      >
        <Text style={styles.createButtonText}>
          {isLoading ? 'Loading...' : 'Create New Quiz'}
        </Text>
      </TouchableOpacity>
      
      {isLoading ? (
        <Text style={styles.loadingText}>Loading quizzes...</Text>
      ) : (
        <FlatList
          data={quizzes}
          keyExtractor={(item) => item.id}
          renderItem={({ item }) => (
            <View style={styles.quizCard}>
              <Text style={styles.quizTitle}>{item.title}</Text>
              <Text style={styles.quizStatus}>
                Status: {item.published ? 'Published' : 'Draft'}
              </Text>
              <View style={styles.buttonContainer}>
                <TouchableOpacity 
                  style={styles.actionButton} 
                  onPress={() => handleEditQuiz(item)}
                >
                  <Text style={styles.actionButtonText}>Edit</Text>
                </TouchableOpacity>
                
                {!item.published && (
                  <TouchableOpacity 
                    style={[styles.actionButton, styles.publishButton]} 
                    onPress={() => handlePublishQuiz(item.id)}
                  >
                    <Text style={styles.actionButtonText}>Publish</Text>
                  </TouchableOpacity>
                )}
                
                <TouchableOpacity 
                  style={[styles.actionButton, styles.deleteButton]} 
                  onPress={() => handleDeleteQuiz(item.id)}
                >
                  <Text style={styles.actionButtonText}>Delete</Text>
                </TouchableOpacity>
              </View>
            </View>
          )}
          ListEmptyComponent={
            <Text style={styles.emptyText}>No quizzes found. Create your first quiz!</Text>
          }
        />
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    backgroundColor: '#f5f5f5',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
    textAlign: 'center',
  },
  createButton: {
    backgroundColor: '#36B1F0',
    padding: 15,
    borderRadius: 5,
    marginBottom: 20,
    alignItems: 'center',
  },
  createButtonText: {
    color: '#fff',
    fontWeight: 'bold',
  },
  quizCard: {
    backgroundColor: '#fff',
    padding: 15,
    borderRadius: 5,
    marginBottom: 15,
    borderWidth: 1,
    borderColor: '#ddd',
  },
  quizTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 5,
  },
  quizStatus: {
    color: '#666',
    marginBottom: 10,
  },
  buttonContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  actionButton: {
    padding: 10,
    borderRadius: 5,
    flex: 1,
    marginHorizontal: 5,
    alignItems: 'center',
  },
  publishButton: {
    backgroundColor: '#28A125',
  },
  deleteButton: {
    backgroundColor: '#ff4136',
  },
  actionButtonText: {
    color: '#fff',
  },
  logoutButton: {
    marginRight: 15,
  },
  logoutText: {
    color: '#ff4136',
    fontWeight: 'bold',
  },
  loadingText: {
    textAlign: 'center',
    marginTop: 20,
    color: '#666',
  },
  emptyText: {
    textAlign: 'center',
    marginTop: 20,
    color: '#666',
    fontSize: 16,
  },
});

export default TeacherDashboard;